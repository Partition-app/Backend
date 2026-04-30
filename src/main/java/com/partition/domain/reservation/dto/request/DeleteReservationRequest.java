package com.partition.domain.reservation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class DeleteReservationRequest {

    private List<Long> reservationIds;
}
