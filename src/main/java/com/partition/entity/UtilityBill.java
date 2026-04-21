package com.partition.entity;

import com.partition.entity.enums.BillCategoryType;
import com.partition.entity.enums.BillStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(20)")
    private BillStatus status;

    @Column(length = 255)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    @Builder
    public UtilityBill(Household household, BillCategoryType billType, LocalDate dueDate, Integer amount, String note) {
        this.household = household;
        this.billType = billType;
        this.dueDate = dueDate;
        this.amount = amount;
        this.status = BillStatus.UNSETTLED;
        this.note = note;
    }

    public void requestSettlement(Settlement settlement) {
        this.status = BillStatus.REQUESTED;
        this.settlement = settlement;
    }

    public void settle(Settlement settlement) {
        this.status = BillStatus.SETTLED;
        this.settlement = settlement;
    }
}