package com.partition.domain.utilitybill.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BillSettlementDetailResponse {

    private Long settlementId;
    private Integer totalAmount;
    private Integer amountPerMember;
    private Integer memberCount;
    private Boolean isConfirmed;
    private LocalDateTime confirmedAt;
    private List<MemberItem> members;
    private List<BillItem> bills;

    @Getter
    @Builder
    public static class MemberItem {
        private Long userId;
        private String name;
        private Integer amount;
    }

    @Getter
    @Builder
    public static class BillItem {
        private Long billId;
        private String utilityTypeName;
        private Integer payDay;
        private Integer amount;
    }
}
