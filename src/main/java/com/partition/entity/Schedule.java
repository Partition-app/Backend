package com.partition.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Table(name = "schedules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDate date;

    private LocalTime time; // 시간은 선택 (null 가능)

    @Builder
    public Schedule(User user, String content, LocalDate date, LocalTime time) {
        this.user = user;
        this.content = content;
        this.date = date;
        this.time = time;
    }

    // 내용 수정 메서드
    public void update(String content, LocalDate date, LocalTime time) {
        if (content != null) this.content = content;
        if (date != null) this.date = date;
        // 시간은 null로 업데이트(삭제)하고 싶을 수도 있으므로 그대로 할당
        this.time = time;
    }
}