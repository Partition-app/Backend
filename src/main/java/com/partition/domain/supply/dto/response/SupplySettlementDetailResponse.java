package com.partition.domain.supply.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SupplySettlementDetailResponse {

    private Long settlementId;
    private Integer totalAmount;
    private Integer amountPerMember;
    private Integer memberCount;
    private Boolean isConfirmed;
    private LocalDateTime confirmedAt;
    private List<MemberItem> members;
    private List<PurchaseItem> items;

    @Getter
    @Builder
    public static class MemberItem {
        private Long userId;
        private String name;
        private Integer amount;
    }

    @Getter
    @Builder
    public static class PurchaseItem {
        private Long purchaseId;
        private String itemName;
        private LocalDate purchaseDate;
        private Integer amount;
    }
}
