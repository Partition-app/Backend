package com.partition.domain.utilitybill.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.partition.entity.UtilityBill;
import com.partition.entity.UtilityBillPayment;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BillPaymentHistoryResponse {

    private Long billId;
    private String utilityType;
    private String utilityTypeName;
    @JsonProperty("isFixed")
    private boolean isFixed;
    private Integer payDay;
    private List<PaymentItem> payments;

    @Getter
    @Builder
    public static class PaymentItem {
        private Long paymentId;
        private String yearMonth;
        private Integer amount;
        private String status;

        public static PaymentItem from(UtilityBillPayment payment) {
            return PaymentItem.builder()
                    .paymentId(payment.getId())
                    .yearMonth(payment.getYearMonth())
                    .amount(payment.getAmount())
                    .status(payment.getStatus().name())
                    .build();
        }
    }

    public static BillPaymentHistoryResponse of(UtilityBill bill, List<UtilityBillPayment> payments) {
        return BillPaymentHistoryResponse.builder()
                .billId(bill.getId())
                .utilityType(bill.getBillType().name())
                .utilityTypeName(bill.getBillType().getLabel())
                .isFixed(bill.isFixed())
                .payDay(bill.getPayDay())
                .payments(payments.stream().map(PaymentItem::from).toList())
                .build();
    }
}
