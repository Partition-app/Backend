package com.partition.domain.alarm.dto.response;

import com.partition.entity.Alarm;
import com.partition.entity.enums.AlarmType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AlarmListResponse {

    private List<AlarmItem> alarms;
    private long unreadCount;

    @Getter
    @Builder
    public static class AlarmItem {
        private Long alarmId;
        private AlarmType type;
        private String message;
        private Long referenceId;
        private Boolean isRead;
        private LocalDateTime createdAt;

        public static AlarmItem from(Alarm alarm) {
            return AlarmItem.builder()
                    .alarmId(alarm.getId())
                    .type(alarm.getType())
                    .message(alarm.getMessage())
                    .referenceId(alarm.getReferenceId())
                    .isRead(alarm.getIsRead())
                    .createdAt(alarm.getCreatedAt())
                    .build();
        }
    }
}