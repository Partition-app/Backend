package com.partition.domain.report.service;

import com.partition.domain.chore.repository.ChoreRepository;
import com.partition.domain.chore.repository.HouseholdChoreRepository;
import com.partition.domain.preference.repository.UserChorePreferenceRepository;
import com.partition.entity.UserChorePreference;
import com.partition.domain.report.dto.response.PartitionReportResponse;
import com.partition.domain.report.dto.response.SettlementReportResponse;
import com.partition.domain.report.dto.response.PartitionReportResponse.BillReport;
import com.partition.domain.report.dto.response.PartitionReportResponse.ChoreReport;
import com.partition.domain.report.dto.response.PartitionReportResponse.Performer;
import com.partition.domain.report.dto.response.PartitionReportResponse.ReservationReport;
import com.partition.domain.report.exception.ReportErrorCode;
import com.partition.domain.reservation.repository.ReservationRepository;
import com.partition.domain.supply.repository.SupplyPurchaseRepository;
import com.partition.domain.user.exception.UserErrorCode;
import com.partition.domain.user.repository.UserRepository;
import com.partition.domain.utilitybill.repository.UtilityBillRepository;
import com.partition.entity.Chore;
import com.partition.entity.HouseholdChore;
import com.partition.entity.Reservation;
import com.partition.entity.SupplyPurchase;
import com.partition.entity.UtilityBill;
import com.partition.entity.enums.BillCategoryType;
import com.partition.entity.enums.BillStatus;
import com.partition.entity.enums.ChoreType;
import com.partition.entity.enums.SupplyPurchaseStatus;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final ChoreRepository choreRepository;
    private final HouseholdChoreRepository householdChoreRepository;
    private final UserChorePreferenceRepository userChorePreferenceRepository;
    private final SupplyPurchaseRepository supplyPurchaseRepository;
    private final UtilityBillRepository utilityBillRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public PartitionReportResponse getReport(Long userId, String startDateStr, String endDateStr) {
        LocalDate start = parseDate(startDateStr, ReportErrorCode.REPORT_1001);
        LocalDate end = parseDate(endDateStr, ReportErrorCode.REPORT_1002);

        if (!end.isAfter(start)) {
            throw new CustomException(ReportErrorCode.REPORT_1004);
        }

        Long householdId = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND))
                .getHouseholdId();

        if (householdId == null) {
            throw new CustomException(ReportErrorCode.REPORT_1005);
        }

        return PartitionReportResponse.builder()
                .chores(buildChoreReports(householdId, start, end))
                .supplies(buildSupplyReport(householdId, start, end))
                .bills(buildBillReports(householdId, start, end))
                .reservations(buildReservationReports(householdId, start, end))
                .build();
    }

    private List<ChoreReport> buildChoreReports(Long householdId, LocalDate start, LocalDate end) {
        // 멤버별 난이도 선호도의 평균. 선호도 없는 타입은 HouseholdChore.difficulty로 fallback
        Map<ChoreType, Integer> defaultDifficultyMap = householdChoreRepository.findByHouseholdId(householdId)
                .stream()
                .collect(Collectors.toMap(HouseholdChore::getChoreType, HouseholdChore::getDifficulty));

        Map<ChoreType, Double> avgScoreMap = userChorePreferenceRepository.findAllByHouseholdId(householdId)
                .stream()
                .collect(Collectors.groupingBy(UserChorePreference::getChoreType,
                        Collectors.averagingInt(UserChorePreference::getScore)));

        Map<ChoreType, Float> difficultyMap = defaultDifficultyMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> avgScoreMap.containsKey(e.getKey())
                                ? avgScoreMap.get(e.getKey()).floatValue()
                                : e.getValue().floatValue()
                ));

        List<Chore> completedChores = choreRepository.findCompletedByHouseholdIdAndDateRange(householdId, start, end);
        Map<ChoreType, List<Chore>> byType = completedChores.stream()
                .collect(Collectors.groupingBy(Chore::getType));

        LocalDate today = LocalDate.now();

        return difficultyMap.entrySet().stream()
                .map(entry -> {
                    ChoreType choreType = entry.getKey();
                    List<Chore> chores = byType.getOrDefault(choreType, List.of());

                    Performer top = null;
                    Performer bottom = null;
                    Integer lastPerformedDaysAgo = null;

                    if (!chores.isEmpty()) {
                        Map<Long, Long> countByUser = chores.stream()
                                .collect(Collectors.groupingBy(c -> c.getAssignee().getId(), Collectors.counting()));

                        top = countByUser.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .map(e -> buildPerformerFromChores(e.getKey(), chores, e.getValue()))
                                .orElse(null);

                        bottom = countByUser.entrySet().stream()
                                .min(Map.Entry.comparingByValue())
                                .map(e -> buildPerformerFromChores(e.getKey(), chores, e.getValue()))
                                .orElse(null);
                    }

                    Integer daysAgo = chores.stream()
                            .map(Chore::getDate)
                            .max(Comparator.naturalOrder())
                            .map(d -> (int) ChronoUnit.DAYS.between(d, today))
                            .orElse(null);

                    return ChoreReport.builder()
                            .choreType(choreType.name())
                            .choreName(choreType.getDescription())
                            .totalCount(chores.size())
                            .avgDifficulty(entry.getValue())
                            .topPerformer(top)
                            .bottomPerformer(bottom)
                            .lastPerformedDaysAgo(daysAgo)
                            .build();
                })
                .toList();
    }

    private Performer buildPerformerFromChores(Long userId, List<Chore> chores, long count) {
        String name = chores.stream()
                .filter(c -> c.getAssignee().getId().equals(userId))
                .findFirst()
                .map(c -> c.getAssignee().getName())
                .orElse("");
        return Performer.builder().userId(userId).userName(name).count((int) count).build();
    }

    private PartitionReportResponse.SupplyReport buildSupplyReport(Long householdId, LocalDate start, LocalDate end) {
        List<SupplyPurchase> purchases = supplyPurchaseRepository
                .findAllByHouseholdIdAndPurchaseDateBetweenOrderByPurchaseDateAscIdAsc(householdId, start, end);

        PartitionReportResponse.HighestAmountItem highestAmountItem = purchases.stream()
                .max(Comparator.comparingInt(SupplyPurchase::getAmount))
                .map(p -> PartitionReportResponse.HighestAmountItem.builder()
                        .itemName(p.getItemName())
                        .amount(p.getAmount())
                        .build())
                .orElse(null);

        PartitionReportResponse.MostPurchasedItem mostPurchasedItem = purchases.stream()
                .collect(Collectors.groupingBy(SupplyPurchase::getItemName, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> PartitionReportResponse.MostPurchasedItem.builder()
                        .itemName(e.getKey())
                        .purchaseCount(e.getValue().intValue())
                        .build())
                .orElse(null);

        return PartitionReportResponse.SupplyReport.builder()
                .highestAmountItem(highestAmountItem)
                .mostPurchasedItem(mostPurchasedItem)
                .build();
    }

    private List<BillReport> buildBillReports(Long householdId, LocalDate start, LocalDate end) {
        long durationDays = ChronoUnit.DAYS.between(start, end);
        LocalDate prevEnd = start.minusDays(1);
        LocalDate prevStart = prevEnd.minusDays(durationDays);

        Map<BillCategoryType, Integer> currentByType = utilityBillRepository
                .findAllByHouseholdIdAndDueDateBetween(householdId, start, end)
                .stream()
                .collect(Collectors.groupingBy(UtilityBill::getBillType,
                        Collectors.summingInt(UtilityBill::getAmount)));

        Map<BillCategoryType, Integer> previousByType = utilityBillRepository
                .findAllByHouseholdIdAndDueDateBetween(householdId, prevStart, prevEnd)
                .stream()
                .collect(Collectors.groupingBy(UtilityBill::getBillType,
                        Collectors.summingInt(UtilityBill::getAmount)));

        return currentByType.entrySet().stream()
                .filter(e -> previousByType.containsKey(e.getKey()) && previousByType.get(e.getKey()) > 0)
                .map(e -> {
                    BillCategoryType type = e.getKey();
                    int current = e.getValue();
                    int previous = previousByType.get(type);
                    float changeRate = (float)(Math.round((double)(current - previous) / previous * 1000) / 10.0);
                    return BillReport.builder()
                            .utilityType(type.name())
                            .utilityTypeName(type.getLabel())
                            .previousAmount(previous)
                            .currentAmount(current)
                            .changeRate(changeRate)
                            .build();
                })
                .filter(r -> Math.abs(r.getChangeRate()) >= 10.0f)
                .toList();
    }

    private List<ReservationReport> buildReservationReports(Long householdId, LocalDate start, LocalDate end) {
        List<Reservation> reservations = reservationRepository.findByHouseholdIdAndDateRange(
                householdId, start.atStartOfDay(), end.plusDays(1).atStartOfDay());

        return reservations.stream()
                .collect(Collectors.groupingBy(r -> r.getItem().getId()))
                .values().stream()
                .map(itemReservations -> {
                    String itemName = itemReservations.get(0).getItem().getName();

                    Map<Long, Long> countByUser = itemReservations.stream()
                            .collect(Collectors.groupingBy(r -> r.getUser().getId(), Collectors.counting()));

                    Performer top = countByUser.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(e -> buildPerformerFromReservations(e.getKey(), itemReservations, e.getValue()))
                            .orElse(null);

                    Performer bottom = countByUser.entrySet().stream()
                            .min(Map.Entry.comparingByValue())
                            .map(e -> buildPerformerFromReservations(e.getKey(), itemReservations, e.getValue()))
                            .orElse(null);

                    int avgMinutes = (int) itemReservations.stream()
                            .mapToLong(r -> ChronoUnit.MINUTES.between(r.getStartTime(), r.getEndTime()))
                            .average()
                            .orElse(0);

                    return ReservationReport.builder()
                            .itemName(itemName)
                            .topPerformer(top)
                            .bottomPerformer(bottom)
                            .avgDurationMinutes(avgMinutes)
                            .build();
                })
                .toList();
    }

    private Performer buildPerformerFromReservations(Long userId, List<Reservation> reservations, long count) {
        String name = reservations.stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .findFirst()
                .map(r -> r.getUser().getName())
                .orElse("");
        return Performer.builder().userId(userId).userName(name).count((int) count).build();
    }

    @Transactional(readOnly = true)
    public SettlementReportResponse getSettlementReport(Long userId, String startDateStr, String endDateStr) {
        LocalDate start = parseDate(startDateStr, ReportErrorCode.REPORT_1001);
        LocalDate end = parseDate(endDateStr, ReportErrorCode.REPORT_1002);

        if (!end.isAfter(start)) {
            throw new CustomException(ReportErrorCode.REPORT_1004);
        }

        Long householdId = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND))
                .getHouseholdId();

        if (householdId == null) {
            throw new CustomException(ReportErrorCode.REPORT_1005);
        }

        List<SettlementReportResponse.SupplyItem> supplies = supplyPurchaseRepository
                .findAllByHouseholdIdAndStatusAndPurchaseDateBetweenOrderByPurchaseDateAscIdAsc(
                        householdId, SupplyPurchaseStatus.SETTLED, start, end)
                .stream()
                .map(p -> SettlementReportResponse.SupplyItem.builder()
                        .itemName(p.getItemName())
                        .purchaseDate(p.getPurchaseDate())
                        .amount(p.getAmount())
                        .quantity(p.getQuantity())
                        .build())
                .toList();

        List<SettlementReportResponse.BillItem> bills = utilityBillRepository
                .findAllByHouseholdIdAndStatusAndDueDateBetweenOrderByDueDateAscIdAsc(
                        householdId, BillStatus.SETTLED, start, end)
                .stream()
                .map(b -> SettlementReportResponse.BillItem.builder()
                        .utilityType(b.getBillType().name())
                        .utilityTypeName(b.getBillType().getLabel())
                        .billingMonth(b.getDueDate().toString().substring(0, 7))
                        .amount(b.getAmount())
                        .build())
                .toList();

        return SettlementReportResponse.builder()
                .supplies(supplies)
                .bills(bills)
                .build();
    }

    private LocalDate parseDate(String date, ReportErrorCode missingErrorCode) {
        if (date == null || date.isBlank()) {
            throw new CustomException(missingErrorCode);
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new CustomException(ReportErrorCode.REPORT_1003);
        }
    }
}
