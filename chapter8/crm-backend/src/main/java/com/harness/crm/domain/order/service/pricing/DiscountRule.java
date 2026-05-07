package com.harness.crm.domain.order.service.pricing;

import java.math.BigDecimal;

/**
 * 折扣策略接口，新增折扣规则只需实现此接口并标注 @Component + @Order
 */
public interface DiscountRule {

    /**
     * 判断当前规则是否命中
     */
    boolean matches(PricingContext context);

    /**
     * 命中后返回折扣率（如 0.10 表示 10%）
     */
    BigDecimal getRate(PricingContext context);
}
