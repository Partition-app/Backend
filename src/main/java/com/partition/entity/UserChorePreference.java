package com.partition.entity;

import com.partition.entity.enums.ChoreType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_chore_preferences")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserChorePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChoreType choreType;

    @Column(nullable = false)
    private Integer score; // 1 ~ 5 점

    @Builder
    public UserChorePreference(User user, ChoreType choreType, Integer score) {
        this.user = user;
        this.choreType = choreType;
        this.score = score;
    }
}