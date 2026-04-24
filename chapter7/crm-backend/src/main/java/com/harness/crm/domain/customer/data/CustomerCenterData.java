package com.harness.crm.domain.customer.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCenterData {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String idType;
    private String idNumber;
    private String gender;
    private String birthday;
    private String occupation;
    private String accountStatus;
    private String memberLevel;
    private String authLevel;
    private String idPhotoUrl;
    private String riskProfile;
    private String incomeRange;
    private BigDecimal aum;
    private BigDecimal availableBalance;
    private BigDecimal totalReturn;
    private String freezeReason;
    private String unfreezeReason;
    private String createTime;
    private String updateTime;
    private List<HoldingProduct> holdingProducts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoldingProduct {
        private String productType;
        private String productCode;
        private String productName;
        private BigDecimal amount;
    }
}
