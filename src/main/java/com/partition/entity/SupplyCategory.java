package com.partition.entity;

import com.partition.entity.enums.SupplyCategoryType;
import com.partition.entity.enums.SupplySubCategoryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "supply_categories",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_supply_category_household_sub_category",
                columnNames = {"household_id", "sub_category"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupplyCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supply_category_id")
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

    @Builder
    public SupplyCategory(Household household, SupplyCategoryType category, SupplySubCategoryType subCategory) {
        this.household = household;
        this.category = category;
        this.subCategory = subCategory;
    }
}
