package com.partition.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "settlements")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "member_count", nullable = false)
    private Integer memberCount;

    @Column(name = "amount_per_member", nullable = false)
    private Integer amountPerMember;

    @Builder
    public Settlement(Household household, Integer totalAmount, Integer memberCount, Integer amountPerMember) {
        this.household = household;
        this.totalAmount = totalAmount;
        this.memberCount = memberCount;
        this.amountPerMember = amountPerMember;
    }
}
