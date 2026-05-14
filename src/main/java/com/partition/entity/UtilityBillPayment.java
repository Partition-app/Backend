package com.partition.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    name = "utility_bill_payments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"bill_id", "year_month"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UtilityBillPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private UtilityBill bill;

    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth;

    @Column
    private Integer amount;

    @Builder
    public UtilityBillPayment(UtilityBill bill, String yearMonth, Integer amount) {
        this.bill = bill;
        this.yearMonth = yearMonth;
        this.amount = amount;
    }

    public void updateAmount(Integer amount) {
        this.amount = amount;
    }
}
