package com.partition.domain.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PartitionReportResponse {

    private List<ChoreReport> chores;
    private SupplyReport supplies;
    private List<BillReport> bills;
    private List<ReservationReport> reservations;

    @Getter
    @Builder
    public static class ChoreReport {
        private String choreType;
        private String choreName;
        private Integer totalCount;
        private Float avgDifficulty;
        private Performer topPerformer;
        private Performer bottomPerformer;
        private Integer lastPerformedDaysAgo;
    }

    @Getter
    @Builder
    public static class Performer {
        private Long userId;
        private String userName;
        private Integer count;
    }

    @Getter
    @Builder
    public static class SupplyReport {
        private HighestAmountItem highestAmountItem;
        private MostPurchasedItem mostPurchasedItem;
    }

    @Getter
    @Builder
    public static class HighestAmountItem {
        private String itemName;
        private Integer amount;
    }

    @Getter
    @Builder
    public static class MostPurchasedItem {
        private String itemName;
        private Integer purchaseCount;
    }

    @Getter
    @Builder
    public static class BillReport {
        private String utilityType;
        private String utilityTypeName;
        private Integer previousAmount;
        private Integer currentAmount;
        private Float changeRate;
    }

    @Getter
    @Builder
    public static class ReservationReport {
        private String itemName;
        private Performer topPerformer;
        private Performer bottomPerformer;
        private Integer avgDurationMinutes;
    }
}
