package com.partition.entity;

import com.partition.entity.enums.BillCategoryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "utility_bills")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UtilityBill extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Enumerated(EnumType.STRING)
    @Column(name = "bill_type", nullable = false, columnDefinition = "varchar(50)")
    private BillCategoryType billType;

    @Column(name = "pay_day", nullable = false)
    private Integer payDay;

    @Column(name = "is_fixed", nullable = false)
    private boolean isFixed;

    @Column
    private Integer amount;

    @Column(length = 255)
    private String note;

    @Builder
    public UtilityBill(Household household, BillCategoryType billType, Integer payDay, boolean isFixed, Integer amount, String note) {
        this.household = household;
        this.billType = billType;
        this.payDay = payDay;
        this.isFixed = isFixed;
        this.amount = amount;
        this.note = note;
    }

    public void update(BillCategoryType billType, Integer payDay, boolean isFixed, Integer amount, String note) {
        this.billType = billType;
        this.payDay = payDay;
        this.isFixed = isFixed;
        this.amount = amount;
        this.note = note;
    }
}
