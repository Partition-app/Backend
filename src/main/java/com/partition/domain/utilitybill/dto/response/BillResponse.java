package com.partition.domain.utilitybill.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.partition.entity.UtilityBill;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BillResponse {

    private Long billId;
    private String utilityType;
    private String utilityTypeName;
    private Integer payDay;
    @JsonProperty("isFixed")
    private boolean isFixed;
    private Integer amount;
    private Integer thisMonthAmount;
    private String note;
    private String status;
    private LocalDateTime createdAt;

    public static BillResponse from(UtilityBill bill, Integer thisMonthAmount) {
        return BillResponse.builder()
                .billId(bill.getId())
                .utilityType(bill.getBillType().name())
                .utilityTypeName(bill.getBillType().getLabel())
                .payDay(bill.getPayDay())
                .isFixed(bill.isFixed())
                .amount(bill.getAmount())
                .thisMonthAmount(thisMonthAmount)
                .note(bill.getNote())
                .status(bill.getStatus().name())
                .createdAt(bill.getCreatedAt())
                .build();
    }
}
