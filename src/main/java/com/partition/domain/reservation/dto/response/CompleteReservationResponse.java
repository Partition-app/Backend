package com.partition.domain.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompleteReservationResponse {

    private Long reservationId;

    @JsonProperty("isCompleted")
    private boolean isCompleted;
}
