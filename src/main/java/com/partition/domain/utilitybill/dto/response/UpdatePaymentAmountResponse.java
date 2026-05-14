package com.partition.domain.utilitybill.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdatePaymentAmountResponse {

    private Long billId;
    private String yearMonth;
    private Integer thisMonthAmount;
}
