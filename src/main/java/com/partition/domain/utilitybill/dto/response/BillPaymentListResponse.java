package com.partition.domain.utilitybill.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BillPaymentListResponse {

    private int totalCount;
    private List<PaymentItem> payments;

    @Getter
    @Builder
    public static class PaymentItem {
        private Long paymentId;
        private Long billId;
        private String utilityType;
        private String utilityTypeName;
        @JsonProperty("isFixed")
        private boolean isFixed;
        private Integer payDay;
        private String dueDate;
        private Integer amount;
        private String status;
    }
}
