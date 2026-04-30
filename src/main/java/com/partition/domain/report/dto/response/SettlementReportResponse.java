package com.partition.domain.report.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class SettlementReportResponse {

    private List<SupplyItem> supplies;
    private List<BillItem> bills;

    @Getter
    @Builder
    public static class SupplyItem {
        private String itemName;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate purchaseDate;

        private Integer amount;
        private Integer quantity;
    }

    @Getter
    @Builder
    public static class BillItem {
        private String utilityType;
        private String utilityTypeName;
        private String billingMonth;
        private Integer amount;
    }
}
