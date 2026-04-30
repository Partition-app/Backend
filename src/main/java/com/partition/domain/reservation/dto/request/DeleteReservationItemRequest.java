package com.partition.domain.reservation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class DeleteReservationItemRequest {

    private List<Long> itemIds;
}
