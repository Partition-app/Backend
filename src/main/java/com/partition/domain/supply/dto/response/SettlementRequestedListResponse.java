package com.partition.domain.supply.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SettlementRequestedListResponse {

    private int totalCount;
    private List<SettlementItem> settlements;

    @Getter
    @Builder
    public static class SettlementItem {
        private Long settlementId;
        private Integer totalAmount;
        private Integer amountPerMember;
        private Integer memberCount;
        private LocalDateTime requestedAt;
    }
}
