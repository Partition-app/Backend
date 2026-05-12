package com.partition.domain.alarm.service;

import com.partition.domain.alarm.dto.response.AlarmListResponse;
import com.partition.domain.alarm.dto.response.ReadAlarmResponse;
import com.partition.domain.alarm.exception.AlarmErrorCode;
import com.partition.domain.alarm.repository.AlarmRepository;
import com.partition.entity.Alarm;
import com.partition.entity.SettlementMember;
import com.partition.entity.enums.AlarmType;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final FcmService fcmService;

    @Transactional(readOnly = true)
    public AlarmListResponse getAlarms(Long userId) {
        List<Alarm> alarms = alarmRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        long unreadCount = alarmRepository.countByUserIdAndIsReadFalse(userId);

        return AlarmListResponse.builder()
                .alarms(alarms.stream().map(AlarmListResponse.AlarmItem::from).toList())
                .unreadCount(unreadCount)
                .build();
    }

    @Transactional
    public ReadAlarmResponse readAlarm(Long userId, Long alarmId) {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new CustomException(AlarmErrorCode.ALARM_1001));

        if (!alarm.getUserId().equals(userId)) {
            throw new CustomException(AlarmErrorCode.ALARM_1002);
        }

        alarm.read();

        return ReadAlarmResponse.builder()
                .alarmId(alarm.getId())
                .isRead(alarm.getIsRead())
                .build();
    }

    @Transactional
    public void deleteAlarm(Long userId, Long alarmId) {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new CustomException(AlarmErrorCode.ALARM_1001));

        if (!alarm.getUserId().equals(userId)) {
            throw new CustomException(AlarmErrorCode.ALARM_1002);
        }

        alarmRepository.delete(alarm);
    }

    @Transactional
    public void createSettlementAlarms(List<SettlementMember> members, Long settlementId, AlarmType type) {
        List<Alarm> alarms = members.stream()
                .map(member -> Alarm.builder()
                        .userId(member.getUser().getId())
                        .type(type)
                        .referenceId(settlementId)
                        .build())
                .toList();

        alarmRepository.saveAll(alarms);

        members.stream()
                .filter(member -> member.getUser().getFcmToken() != null)
                .forEach(member -> fcmService.sendPush(member.getUser().getFcmToken(), type));
    }
}
