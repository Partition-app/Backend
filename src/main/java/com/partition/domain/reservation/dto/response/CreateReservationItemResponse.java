package com.partition.domain.reservation.dto.response;

import com.partition.entity.ReservationItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CreateReservationItemResponse {

    private Long itemId;
    private String name;
    private LocalDateTime createdAt;

    public static CreateReservationItemResponse from(ReservationItem item) {
        return CreateReservationItemResponse.builder()
                .itemId(item.getId())
                .name(item.getName())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
