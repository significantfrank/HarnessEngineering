package com.harness.crm.app.customer.dto;

import com.harness.crm.domain.customer.data.CustomerCenterData;
import com.harness.crm.domain.customer.enums.CcSyncStatus;
import com.harness.crm.domain.customer.enums.CustomerLevel;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
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
public class Customer360DTO {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String idType;
    private String idNumber;
    private CcSyncStatus ccSyncStatus;

    private CustomerSource source;
    private CustomerLevel level;
    private CustomerStatus status;
    private LocalDateTime lastFollowUp;
    private String remark;
    private String company;
    private String address;
    private String industry;
    private String website;
    private String contactPerson;

    private List<TagDTO> tags;
    private List<CustomerNoteDTO> notes;

    private String gender;
    private String birthday;
    private String occupation;
    private String accountStatus;
    private String memberLevel;
    private String authLevel;
    private String riskProfile;
    private String incomeRange;
    private BigDecimal aum;
    private BigDecimal availableBalance;
    private BigDecimal totalReturn;
    private List<HoldingProductItem> holdingProducts;

    private Boolean degraded;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 设置center主数据独有字段 */
    public void fillCenterFields(CustomerCenterData center) {
        if (center == null) return;
        this.gender = center.getGender();
        this.birthday = center.getBirthday();
        this.occupation = center.getOccupation();
        this.accountStatus = center.getAccountStatus();
        this.memberLevel = center.getMemberLevel();
        this.authLevel = center.getAuthLevel();
        this.riskProfile = center.getRiskProfile();
        this.incomeRange = center.getIncomeRange();
        this.aum = center.getAum();
        this.availableBalance = center.getAvailableBalance();
        this.totalReturn = center.getTotalReturn();
        if (center.getHoldingProducts() != null) {
            this.holdingProducts = center.getHoldingProducts().stream()
                    .map(p -> HoldingProductItem.builder()
                            .productType(p.getProductType())
                            .productCode(p.getProductCode())
                            .productName(p.getProductName())
                            .amount(p.getAmount())
                            .build())
                    .toList();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoldingProductItem {
        private String productType;
        private String productCode;
        private String productName;
        private BigDecimal amount;
    }
}
