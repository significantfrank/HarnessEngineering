package com.harness.customer.domain.entity;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldingProduct {

    private ProductType productType;

    private String productCode;

    private String productName;

    private BigDecimal amount;
}
