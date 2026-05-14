package com.partition.domain.homeshare.service;

import com.partition.domain.alarm.service.FcmService;
import com.partition.domain.homeshare.dto.response.HomeLocationResponse;
import com.partition.domain.homeshare.dto.response.LocationConsentResponse;
import com.partition.domain.homeshare.dto.response.NearHomeEventResponse;
import com.partition.domain.homeshare.repository.HomeLocationRepository;
import com.partition.domain.homeshare.repository.LocationSharingConsentRepository;
import com.partition.domain.homeshare.repository.NearHomeEventRepository;
import com.partition.domain.household.exception.HouseholdErrorCode;
import com.partition.domain.household.repository.HouseholdRepository;
import com.partition.domain.user.repository.UserRepository;
import com.partition.domain.user.exception.UserErrorCode;
import com.partition.entity.HomeLocation;
import com.partition.entity.Household;
import com.partition.entity.LocationSharingConsent;
import com.partition.entity.NearHomeEvent;
import com.partition.entity.User;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeShareService {

    private static final Duration NOTIFICATION_COOLDOWN = Duration.ofMinutes(30);

    private final LocationSharingConsentRepository consentRepository;
    private final HomeLocationRepository homeLocationRepository;
    private final NearHomeEventRepository nearHomeEventRepository;
    private final FcmService fcmService;
    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;

    @Transactional
    public LocationConsentResponse saveConsent(Long userId, boolean agreed) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Household household = getHouseholdByUser(user);

        LocationSharingConsent consent = consentRepository
                .findByUserAndHousehold(user, household)
                .orElse(null);

        if (consent == null) {
            consent = LocationSharingConsent.create(user, household, agreed);
            consentRepository.save(consent);
        } else {
            consent.update(agreed);
        }

        return LocationConsentResponse.builder()
                .userId(userId)
                .householdId(household.getId())
                .agreed(agreed)
                .build();
    }

    @Transactional
    public HomeLocationResponse saveHomeLocation(Long userId, double lat, double lng, int radius) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Household household = getHouseholdByUser(user);

        HomeLocation homeLocation = homeLocationRepository.findByHousehold(household)
                .orElse(null);

        if (homeLocation == null) {
            homeLocation = HomeLocation.create(household, lat, lng, radius);
            homeLocationRepository.save(homeLocation);
        } else {
            homeLocation.update(lat, lng, radius);
        }

        return HomeLocationResponse.builder()
                .householdId(household.getId())
                .lat(lat)
                .lng(lng)
                .radius(radius)
                .build();
    }

    @Transactional(readOnly = true)
    public LocationConsentResponse getConsent(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Household household = getHouseholdByUser(user);

        boolean agreed = consentRepository.findByUserAndHousehold(user, household)
                .map(LocationSharingConsent::isAgreed)
                .orElse(false);

        return LocationConsentResponse.builder()
                .userId(userId)
                .householdId(household.getId())
                .agreed(agreed)
                .build();
    }

    @Transactional(readOnly = true)
    public HomeLocationResponse getHomeLocation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Household household = getHouseholdByUser(user);

        HomeLocation homeLocation = homeLocationRepository.findByHousehold(household)
                .orElse(null);

        if (homeLocation == null) {
            return HomeLocationResponse.builder()
                    .householdId(household.getId())
                    .lat(0)
                    .lng(0)
                    .radius(300)
                    .build();
        }

        return HomeLocationResponse.builder()
                .householdId(household.getId())
                .lat(homeLocation.getLat().doubleValue())
                .lng(homeLocation.getLng().doubleValue())
                .radius(homeLocation.getRadius())
                .build();
    }

    @Transactional
    public NearHomeEventResponse handleNearHomeEvent(Long userId, String eventType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Household household = getHouseholdByUser(user);

        // 본인이 위치 공유에 동의했는지 확인
        boolean hasConsent = consentRepository.findByUserAndHousehold(user, household)
                .map(LocationSharingConsent::isAgreed)
                .orElse(false);

        if (!hasConsent) {
            return NearHomeEventResponse.builder().notifiedUserCount(0).build();
        }

        // 30분 쿨다운 확인
        LocalDateTime cutoff = LocalDateTime.now().minus(NOTIFICATION_COOLDOWN);
        List<NearHomeEvent> recentEvents = nearHomeEventRepository.findRecentEvents(user, household, cutoff);

        if (!recentEvents.isEmpty()) {
            return NearHomeEventResponse.builder().notifiedUserCount(0).build();
        }

        // 이벤트 기록 저장
        nearHomeEventRepository.save(NearHomeEvent.create(user, household, eventType));

        // 동의한 룸메이트 조회
        List<LocationSharingConsent> recipients = consentRepository.findAgreedMembersExcluding(household, user);

        // FCM 발송
        int notifiedCount = 0;
        for (LocationSharingConsent consent : recipients) {
            String fcmToken = consent.getUser().getFcmToken();
            if (fcmToken != null && !fcmToken.isBlank()) {
                fcmService.sendNearHomeNotification(fcmToken, user.getName());
                notifiedCount++;
            }
        }

        return NearHomeEventResponse.builder().notifiedUserCount(notifiedCount).build();
    }

    private Household getHouseholdByUser(User user) {
        if (user.getHouseholdId() == null) {
            throw new CustomException(HouseholdErrorCode.HOUSEHOLD_4001);
        }
        return householdRepository.findById(user.getHouseholdId())
                .orElseThrow(() -> new CustomException(HouseholdErrorCode.HOUSEHOLD_NOT_FOUND));
    }
}
