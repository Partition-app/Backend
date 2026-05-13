package com.partition.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "near_home_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NearHomeEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    public static NearHomeEvent create(User user, Household household, String eventType) {
        NearHomeEvent event = new NearHomeEvent();
        event.user = user;
        event.household = household;
        event.eventType = eventType;
        return event;
    }
}
