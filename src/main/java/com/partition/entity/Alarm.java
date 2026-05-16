package com.partition.entity;

import com.partition.entity.enums.AlarmType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "alarms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alarm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlarmType type;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Builder
    public Alarm(Long userId, AlarmType type, Long referenceId, String message) {
        this.userId = userId;
        this.type = type;
        this.referenceId = referenceId;
        this.message = message != null ? message : type.getMessage();
        this.isRead = false;
    }

    public void read() {
        this.isRead = true;
    }
}