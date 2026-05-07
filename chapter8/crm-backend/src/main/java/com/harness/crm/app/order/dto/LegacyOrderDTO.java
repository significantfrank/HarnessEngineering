package com.harness.crm.app.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegacyOrderDTO {

    private Long customerId;

    private Long opportunityId;

    private BigDecimal totalAmount;

    private String email;

    private String customerLevel;

    private String productType;

    private String ownerName;

    private String remark;
}
