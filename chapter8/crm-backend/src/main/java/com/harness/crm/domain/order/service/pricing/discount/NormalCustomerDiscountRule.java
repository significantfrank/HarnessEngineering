package com.harness.crm.domain.order.service.pricing.discount;

import com.harness.crm.domain.order.service.pricing.DiscountRule;
import com.harness.crm.domain.order.service.pricing.PricingContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 普通客户折扣：NORMAL → 1%
 */
@Component
@Order(5)
public class NormalCustomerDiscountRule implements DiscountRule {

    @Override
    public boolean matches(PricingContext context) {
        return "NORMAL".equals(context.getCustomerLevel());
    }

    @Override
    public BigDecimal getRate(PricingContext context) {
        return new BigDecimal("0.01");
    }
}
