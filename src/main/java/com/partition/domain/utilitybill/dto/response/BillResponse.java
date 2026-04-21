package com.partition.domain.utilitybill.dto.response;

import com.partition.entity.UtilityBill;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class BillResponse {

    private Long billId;
    private String utilityType;
    private String utilityTypeName;
    private LocalDate date;
    private Integer amount;
    private String note;
    private LocalDateTime createdAt;

    public static BillResponse from(UtilityBill bill) {
        return BillResponse.builder()
                .billId(bill.getId())
                .utilityType(bill.getBillType().name())
                .utilityTypeName(bill.getBillType().getLabel())
                .date(bill.getDueDate())
                .amount(bill.getAmount())
                .note(bill.getNote())
                .createdAt(bill.getCreatedAt())
                .build();
    }
}