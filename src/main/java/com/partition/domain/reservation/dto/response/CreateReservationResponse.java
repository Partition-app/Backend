package com.partition.domain.reservation.dto.response;

import com.partition.entity.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CreateReservationResponse {

    private Long reservationId;
    private Long itemId;
    private String itemName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservedBy reservedBy;

    @Getter
    @Builder
    public static class ReservedBy {
        private Long userId;
        private String name;
    }

    public static CreateReservationResponse from(Reservation reservation) {
        return CreateReservationResponse.builder()
                .reservationId(reservation.getId())
                .itemId(reservation.getItem().getId())
                .itemName(reservation.getItem().getName())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .reservedBy(ReservedBy.builder()
                        .userId(reservation.getUser().getId())
                        .name(reservation.getUser().getName())
                        .build())
                .build();
    }
}
