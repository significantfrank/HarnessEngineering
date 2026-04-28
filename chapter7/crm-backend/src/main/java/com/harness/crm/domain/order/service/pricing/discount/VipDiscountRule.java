package com.harness.crm.domain.order.service.pricing.discount;

import com.harness.crm.domain.order.service.pricing.DiscountRule;
import com.harness.crm.domain.order.service.pricing.PricingContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * VIP客户折扣：VIP → 5%
 */
@Component
@Order(3)
public class VipDiscountRule implements DiscountRule {

    @Override
    public boolean matches(PricingContext context) {
        return "VIP".equals(context.getCustomerLevel());
    }

    @Override
    public BigDecimal getRate(PricingContext context) {
        return new BigDecimal("0.05");
    }
}
