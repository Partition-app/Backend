package com.partition.domain.utilitybill.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateBillRequest {

    private String utilityType;
    private Integer payDay;
    private Boolean isFixed;
    private Integer amount;
    private String note;
}