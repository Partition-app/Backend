package com.partition.domain.utilitybill.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateBillSettlementRequest {

    private List<Long> billIds;
    private List<Long> memberIds;
}
