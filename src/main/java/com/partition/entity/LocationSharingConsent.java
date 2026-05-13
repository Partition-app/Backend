package com.partition.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "location_sharing_consents",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "household_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationSharingConsent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(nullable = false)
    private boolean agreed;

    @Column(name = "agreed_at")
    private LocalDateTime agreedAt;

    public static LocationSharingConsent create(User user, Household household, boolean agreed) {
        LocationSharingConsent consent = new LocationSharingConsent();
        consent.user = user;
        consent.household = household;
        consent.agreed = agreed;
        consent.agreedAt = agreed ? LocalDateTime.now() : null;
        return consent;
    }

    public void update(boolean agreed) {
        this.agreed = agreed;
        this.agreedAt = agreed ? LocalDateTime.now() : null;
    }
}
