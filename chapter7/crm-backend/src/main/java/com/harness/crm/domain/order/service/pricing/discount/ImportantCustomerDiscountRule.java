package com.harness.crm.domain.order.service.pricing.discount;

import com.harness.crm.domain.order.service.pricing.DiscountRule;
import com.harness.crm.domain.order.service.pricing.PricingContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 重要客户折扣：IMPORTANT → 3%
 */
@Component
@Order(4)
public class ImportantCustomerDiscountRule implements DiscountRule {

    @Override
    public boolean matches(PricingContext context) {
        return "IMPORTANT".equals(context.getCustomerLevel());
    }

    @Override
    public BigDecimal getRate(PricingContext context) {
        return new BigDecimal("0.03");
    }
}
