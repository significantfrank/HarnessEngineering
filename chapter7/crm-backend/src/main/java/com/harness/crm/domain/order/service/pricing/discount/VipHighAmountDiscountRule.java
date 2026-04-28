package com.harness.crm.domain.order.service.pricing.discount;

import com.harness.crm.domain.order.service.pricing.DiscountRule;
import com.harness.crm.domain.order.service.pricing.PricingContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * VIP高额折扣：VIP + 金额>10000 → 10%
 */
@Component
@Order(2)
public class VipHighAmountDiscountRule implements DiscountRule {

    private static final BigDecimal THRESHOLD = new BigDecimal("10000");

    @Override
    public boolean matches(PricingContext context) {
        return "VIP".equals(context.getCustomerLevel())
                && context.getTotalAmount().compareTo(THRESHOLD) > 0;
    }

    @Override
    public BigDecimal getRate(PricingContext context) {
        return new BigDecimal("0.10");
    }
}
