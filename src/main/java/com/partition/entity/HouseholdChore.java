package com.partition.entity;

import com.partition.entity.enums.ChoreType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "household_chores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseholdChore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChoreType choreType;

    @Column(nullable = false)
    private Integer difficulty; // 1-5 (기본값 3)

    @Column(nullable = false)
    private Integer frequency; // 빈도 (주당 횟수)

    @Builder
    public HouseholdChore(Household household, ChoreType choreType, Integer difficulty, Integer frequency) {
        this.household = household;
        this.choreType = choreType;
        this.difficulty = difficulty;
        this.frequency = frequency;
    }

    // 나중에 난이도 수정 API에서 사용
    public void updateDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public void updateFrequency(Integer frequency) {
        this.frequency = frequency;
    }
}