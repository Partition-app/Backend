package com.partition.entity;

import com.partition.entity.enums.BillStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(20)")
    private BillStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    @Builder
    public UtilityBillPayment(UtilityBill bill, String yearMonth, Integer amount) {
        this.bill = bill;
        this.yearMonth = yearMonth;
        this.amount = amount;
        this.status = BillStatus.UNSETTLED;
    }

    public void updateAmount(Integer amount) {
        this.amount = amount;
    }

    public void requestSettlement(Settlement settlement) {
        this.status = BillStatus.REQUESTED;
        this.settlement = settlement;
    }

    public void settle() {
        this.status = BillStatus.SETTLED;
    }
}
