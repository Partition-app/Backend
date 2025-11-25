package com.partition.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChoreType {
    // 모든 집안일의 기본 난이도를 3으로 설정
    DISH_WASHING("설거지 하기", 3),
    COOKING("요리 하기", 3),
    LAUNDRY("빨래 하기", 3),
    FOODTRASH("음식물 쓰레기 버리기", 3),
    TRASH("일반 쓰레기 버리기", 3),
    RECYCLING("분리수거 하기", 3),
    VACUUM("청소기 돌리기", 3),
    MOPPING("바닥 닦기", 3),
    WINDOW("창문, 창틀 닦기", 3),
    BATHROOM("화장실 청소하기", 3),
    FRIDGE("냉장고 청소하기", 3);

    private final String description;
    private final int defaultDifficulty;
}