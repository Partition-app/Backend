package com.partition.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "utility_bills")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UtilityBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id")
    private Household household;

    private String title; // 월세, 전기세 등

    private LocalDate dueDate; // 납부일

    private boolean isPaid; // 납부 여부
}