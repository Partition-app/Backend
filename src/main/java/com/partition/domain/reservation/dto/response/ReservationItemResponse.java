package com.partition.domain.reservation.dto.response;

import com.partition.entity.ReservationItem;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationItemResponse {

    private Long itemId;
    private String name;

    public static ReservationItemResponse from(ReservationItem item) {
        return ReservationItemResponse.builder()
                .itemId(item.getId())
                .name(item.getName())
                .build();
    }
}
