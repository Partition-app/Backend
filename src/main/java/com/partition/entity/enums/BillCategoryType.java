package com.partition.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BillCategoryType {
    WATER("수도세"),
    ELECTRICITY("전기세"),
    GAS("가스비"),
    INTERNET("인터넷"),
    OTT("OTT"),
    RENT("월세"),
    LOAN_INTEREST("대출이자"),
    ETC("기타");

    private final String label;
}