package com.partition.domain.supply.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CreateSettlementResponse {

    private Long settlementId;
    private Integer totalAmount;
    private Integer memberCount;
    private Integer amountPerMember;
    private LocalDateTime settledAt;
    private List<SettlementMemberResponse> members;
    private List<SettlementItemResponse> items;

    @Getter
    @Builder
    public static class SettlementMemberResponse {
        private Long userId;
        private String name;
        private int amount;
    }

    @Getter
    @Builder
    public static class SettlementItemResponse {
        private Long purchaseId;
        private String itemName;
        private Integer amount;
    }
}
