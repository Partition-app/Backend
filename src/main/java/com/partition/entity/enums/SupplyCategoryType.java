package com.partition.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupplyCategoryType {
    KITCHEN("주방용품"),
    BATHROOM("욕실용품"),
    CLEANING("청소용품"),
    HYGIENE("위생용품"),
    GROCERY("식료품"),
    LIVING("생활용품"),
    ETC("기타");

    private final String label;
}
