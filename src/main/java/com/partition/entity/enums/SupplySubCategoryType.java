package com.partition.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum SupplySubCategoryType {
    SCISSORS("주방가위", SupplyCategoryType.KITCHEN),
    KNIFE("주방칼", SupplyCategoryType.KITCHEN),
    CUTTING_BOARD("도마", SupplyCategoryType.KITCHEN),
    SCRUBBER("수세미", SupplyCategoryType.KITCHEN),
    DISH_SOAP("주방세제", SupplyCategoryType.KITCHEN),
    KITCHEN_TOWEL("키친타올", SupplyCategoryType.KITCHEN),
    GARBAGE_BAG("비닐봉투", SupplyCategoryType.KITCHEN),
    RUBBER_GLOVES("고무장갑", SupplyCategoryType.KITCHEN),
    SHAMPOO("샴푸", SupplyCategoryType.BATHROOM),
    CONDITIONER("린스", SupplyCategoryType.BATHROOM),
    BODY_WASH("바디워시", SupplyCategoryType.BATHROOM),
    TOOTHPASTE("치약", SupplyCategoryType.BATHROOM),
    TOILET_PAPER_BATHROOM("휴지", SupplyCategoryType.BATHROOM),
    HAND_WASH("핸드워시", SupplyCategoryType.BATHROOM),
    CLEANING_SHEET("청소포", SupplyCategoryType.CLEANING),
    CLEANING_BRUSH("청소솔", SupplyCategoryType.CLEANING),
    LAUNDRY_DETERGENT("세탁세제", SupplyCategoryType.CLEANING),
    FABRIC_SOFTENER("섬유유연제", SupplyCategoryType.CLEANING),
    GLASS_CLEANER("유리세정제", SupplyCategoryType.CLEANING),
    BATHROOM_CLEANER("욕실 세제", SupplyCategoryType.CLEANING),
    TOILET_PAPER_HYGIENE("휴지", SupplyCategoryType.HYGIENE),
    WET_WIPES("물티슈", SupplyCategoryType.HYGIENE),
    AIR_FRESHENER_HYGIENE("방향제", SupplyCategoryType.HYGIENE),
    RICE("쌀", SupplyCategoryType.GROCERY),
    WATER("생수", SupplyCategoryType.GROCERY),
    OIL("식용유", SupplyCategoryType.GROCERY),
    BASIC_SEASONING("기본 조미료", SupplyCategoryType.GROCERY),
    BASIC_SAUCE("기본 양념", SupplyCategoryType.GROCERY),
    EGGS("계란", SupplyCategoryType.GROCERY),
    MILK("우유", SupplyCategoryType.GROCERY),
    BREAD("식빵", SupplyCategoryType.GROCERY),
    KIMCHI("김치", SupplyCategoryType.GROCERY),
    RAMEN("라면", SupplyCategoryType.GROCERY),
    VEGETABLES("야채", SupplyCategoryType.GROCERY),
    POWER_STRIP("멀티탭", SupplyCategoryType.ETC),
    TISSUE_BOX("각티슈", SupplyCategoryType.ETC),
    AIR_FRESHENER_ETC("방향제", SupplyCategoryType.ETC),
    FLUORESCENT_LAMP("형광등", SupplyCategoryType.ETC),
    BATTERY("건전지", SupplyCategoryType.ETC),
    MOSQUITO_REPELLENT("모기약", SupplyCategoryType.ETC),
    ELECTRIC_FLY_SWATTER("전기파리채", SupplyCategoryType.ETC);

    private final String label;
    private final SupplyCategoryType categoryType;

    public static List<SupplySubCategoryType> defaults() {
        return Arrays.asList(values());
    }
}
