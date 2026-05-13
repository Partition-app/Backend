package com.partition.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "home_locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeLocation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false, unique = true)
    private Household household;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(nullable = false)
    private int radius;

    public static HomeLocation create(Household household, double lat, double lng, int radius) {
        HomeLocation hl = new HomeLocation();
        hl.household = household;
        hl.lat = BigDecimal.valueOf(lat);
        hl.lng = BigDecimal.valueOf(lng);
        hl.radius = radius;
        return hl;
    }

    public void update(double lat, double lng, int radius) {
        this.lat = BigDecimal.valueOf(lat);
        this.lng = BigDecimal.valueOf(lng);
        this.radius = radius;
    }
}
