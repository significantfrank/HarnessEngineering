package com.harness.crm.domain.order.service.pricing.discount;

import com.harness.crm.domain.order.service.pricing.DiscountRule;
import com.harness.crm.domain.order.service.pricing.PricingContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 高净值客户专属理财折扣：VIP + FUND + 金额>50000 → 8%
 */
@Component
@Order(1)
public class HighNetWorthWealthDiscountRule implements DiscountRule {

    private static final BigDecimal THRESHOLD = new BigDecimal("50000");

    @Override
    public boolean matches(PricingContext context) {
        return "VIP".equals(context.getCustomerLevel())
                && "FUND".equals(context.getProductType())
                && context.getTotalAmount().compareTo(THRESHOLD) > 0;
    }

    @Override
    public BigDecimal getRate(PricingContext context) {
        return new BigDecimal("0.08");
    }
}
