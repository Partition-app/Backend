package com.partition.domain.utilitybill.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BillSettlementListResponse {

    private int totalCount;
    private int totalAmount;
    private int amountPerMember;
    private int remainder;
    private int memberCount;
    private List<BillItem> bills;

    @Getter
    @Builder
    public static class BillItem {
        private Long paymentId;
        private Long billId;
        private String utilityTypeName;
        private String dueDate;
        private Integer amount;
        private String note;
    }
}