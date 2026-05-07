package com.harness.crm.domain.order.service.pricing;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 折扣计算上下文，封装定价所需的请求信息
 */
@Getter
@AllArgsConstructor
public class PricingContext {

    private final String customerLevel;

    private final String productType;

    private final BigDecimal totalAmount;
}
