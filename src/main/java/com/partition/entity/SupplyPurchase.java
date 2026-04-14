package com.partition.entity;

import com.partition.entity.enums.SupplyCategoryType;
import com.partition.entity.enums.SupplySubCategoryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "supply_purchases")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupplyPurchase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, columnDefinition = "varchar(50)")
    private SupplyCategoryType category;

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_category", nullable = false, columnDefinition = "varchar(50)")
    private SupplySubCategoryType subCategory;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "is_settled", nullable = false)
    private Boolean isSettled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    @Builder
    public SupplyPurchase(Household household, SupplyCategoryType category, SupplySubCategoryType subCategory,
                          String itemName, LocalDate purchaseDate, Integer amount, Integer quantity, Boolean isSettled) {
        this.household = household;
        this.category = category;
        this.subCategory = subCategory;
        this.itemName = itemName;
        this.purchaseDate = purchaseDate;
        this.amount = amount;
        this.quantity = quantity;
        this.isSettled = isSettled;
    }

    public void update(String itemName, LocalDate purchaseDate, Integer amount, Integer quantity,
                       SupplyCategoryType category, SupplySubCategoryType subCategory) {
        if (itemName != null) this.itemName = itemName;
        if (purchaseDate != null) this.purchaseDate = purchaseDate;
        if (amount != null) this.amount = amount;
        if (quantity != null) this.quantity = quantity;
        if (category != null) this.category = category;
        if (subCategory != null) this.subCategory = subCategory;
    }

    public void settle(Settlement settlement) {
        this.isSettled = true;
        this.settlement = settlement;
    }
}