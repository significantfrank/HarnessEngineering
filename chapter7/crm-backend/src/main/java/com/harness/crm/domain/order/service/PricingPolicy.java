package com.harness.crm.domain.order.service;

import com.harness.crm.domain.order.service.pricing.DiscountRule;
import com.harness.crm.domain.order.service.pricing.PricingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 定价领域服务：按优先级遍历折扣规则，首个命中即生效
 * 新增折扣规则只需实现 DiscountRule 并标注 @Component + @Order
 */
@Slf4j
@Service
public class PricingPolicy {

    private final List<DiscountRule> rules;

    public PricingPolicy(List<DiscountRule> rules) {
        this.rules = rules;
    }

    /**
     * 计算折扣金额，按规则优先级逐条匹配，首个命中规则决定折扣率
     *
     * @param context 折扣上下文
     * @return 折扣金额，无命中规则时返回零
     */
    public BigDecimal calculate(PricingContext context) {
        return rules.stream()
                .filter(rule -> rule.matches(context))
                .findFirst()
                .map(rule -> {
                    BigDecimal rate = rule.getRate(context);
                    log.info("命中折扣规则: {}, 折扣率: {}", rule.getClass().getSimpleName(), rate);
                    return context.getTotalAmount().multiply(rate);
                })
                .orElse(BigDecimal.ZERO);
    }
}
