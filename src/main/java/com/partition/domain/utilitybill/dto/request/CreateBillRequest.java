package com.partition.domain.utilitybill.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateBillRequest {

    private String utilityType;
    private String dueDate;
    private Integer amount;
    private String note;
}