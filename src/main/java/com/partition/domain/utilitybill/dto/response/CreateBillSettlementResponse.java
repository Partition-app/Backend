package com.partition.domain.utilitybill.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CreateBillSettlementResponse {

    private Long settlementId;
    private Integer totalAmount;
    private Integer memberCount;
    private Integer amountPerMember;
    private LocalDateTime settledAt;
    private List<MemberResponse> members;
    private List<BillItemResponse> items;

    @Getter
    @Builder
    public static class MemberResponse {
        private Long userId;
        private String name;
        private Integer amount;
    }

    @Getter
    @Builder
    public static class BillItemResponse {
        private Long paymentId;
        private Long billId;
        private String utilityTypeName;
        private Integer amount;
    }
}
