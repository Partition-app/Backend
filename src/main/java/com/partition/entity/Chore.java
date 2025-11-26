package com.partition.entity;

import com.partition.entity.enums.ChoreType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "chores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chore extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User assignee; // 담당자

    @Enumerated(EnumType.STRING)
    private ChoreType type;

    private LocalDate date; // 수행 날짜

    private boolean isCompleted; // 완료 여부

    @Builder
    public Chore(User assignee, ChoreType type, LocalDate date) {
        this.assignee = assignee;
        this.type = type;
        this.date = date;
        this.isCompleted = false;
    }
}