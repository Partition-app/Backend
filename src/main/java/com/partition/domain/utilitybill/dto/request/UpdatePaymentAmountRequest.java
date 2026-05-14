package com.partition.domain.utilitybill.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePaymentAmountRequest {

    private Integer thisMonthAmount;
}
