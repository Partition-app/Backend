package com.partition.domain.reservation.dto.response;

import com.partition.entity.ReservationItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UpdateReservationItemResponse {

    private Long itemId;
    private String name;
    private LocalDateTime updatedAt;

    public static UpdateReservationItemResponse from(ReservationItem item) {
        return UpdateReservationItemResponse.builder()
                .itemId(item.getId())
                .name(item.getName())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
