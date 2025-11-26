package com.partition.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChoreType {
    DISH_WASHING("설거지 하기", 3, 7),  // 난이도 3, 주당 7회
    COOKING("요리 하기", 3, 7),
    LAUNDRY("빨래 하기", 3, 3),
    FOODTRASH("음식물 쓰레기 버리기", 3, 4),
    TRASH("일반 쓰레기 버리기", 3, 2),
    RECYCLING("재활용 쓰레기 버리기", 3, 1),
    VACUUM("청소기 돌리기", 3, 3),
    MOPPING("바닥 닦기", 3, 2),
    WINDOW("창문, 창틀 닦기", 3, 1),
    BATHROOM("화장실 청소하기", 3, 1),
    FRIDGE("냉장고 청소하기", 3, 1);

    private final String description;
    private final int defaultDifficulty;
    private final int defaultFrequency;  // 주당 기본 빈도
}