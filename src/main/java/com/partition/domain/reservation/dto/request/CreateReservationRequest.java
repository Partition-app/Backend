package com.partition.domain.reservation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CreateReservationRequest {

    private Long itemId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
