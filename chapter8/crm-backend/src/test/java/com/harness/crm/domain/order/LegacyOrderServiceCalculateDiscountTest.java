package com.harness.crm.domain.order;

import com.harness.crm.domain.order.service.pricing.PricingContext;
import com.harness.crm.domain.order.service.PricingPolicy;
import com.harness.crm.domain.order.service.pricing.discount.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * PricingPolicy 折扣计算单元测试
 * 覆盖所有 DiscountRule 实现及优先级规则
 */
class LegacyOrderServiceCalculateDiscountTest {

    private PricingPolicy pricingPolicy;

    @BeforeEach
    void setUp() {
        pricingPolicy = new PricingPolicy(List.of(
                new HighNetWorthWealthDiscountRule(),
                new VipHighAmountDiscountRule(),
                new VipDiscountRule(),
                new ImportantCustomerDiscountRule(),
                new NormalCustomerDiscountRule()
        ));
    }

    private BigDecimal expectedDiscount(BigDecimal amount, String rateStr) {
        return amount.multiply(new BigDecimal(rateStr)).setScale(2, RoundingMode.HALF_UP);
    }

    @Test
    void shouldApply8Percent_whenVipFundAndAmountExceeds50000() {
        PricingContext ctx = new PricingContext("VIP", "FUND", new BigDecimal("60000"));
        BigDecimal discount = pricingPolicy.calculate(ctx);
        assertEquals(expectedDiscount(new BigDecimal("60000"), "0.08"), discount);
    }

    @Test
    void shouldApply10Percent_whenVipAndAmountExceeds10000() {
        PricingContext ctx = new PricingContext("VIP", "LOAN", new BigDecimal("20000"));
        BigDecimal discount = pricingPolicy.calculate(ctx);
        assertEquals(expectedDiscount(new BigDecimal("20000"), "0.10"), discount);
    }

    @Test
    void shouldApply5Percent_whenVipAndAmountBelowThreshold() {
        PricingContext ctx = new PricingContext("VIP", "LOAN", new BigDecimal("8000"));
        BigDecimal discount = pricingPolicy.calculate(ctx);
        assertEquals(expectedDiscount(new BigDecimal("8000"), "0.05"), discount);
    }

    @Test
    void shouldApply3Percent_whenImportantCustomer() {
        PricingContext ctx = new PricingContext("IMPORTANT", "LOAN", new BigDecimal("8000"));
        BigDecimal discount = pricingPolicy.calculate(ctx);
        assertEquals(expectedDiscount(new BigDecimal("8000"), "0.03"), discount);
    }

    @Test
    void shouldApply1Percent_whenNormalCustomer() {
        PricingContext ctx = new PricingContext("NORMAL", "LOAN", new BigDecimal("8000"));
        BigDecimal discount = pricingPolicy.calculate(ctx);
        assertEquals(expectedDiscount(new BigDecimal("8000"), "0.01"), discount);
    }

    @Test
    void shouldReturnZero_whenPotentialCustomer() {
        PricingContext ctx = new PricingContext("POTENTIAL", "LOAN", new BigDecimal("8000"));
        BigDecimal discount = pricingPolicy.calculate(ctx);
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void shouldApply5Percent_whenVipAndAmountEquals10000() {
        PricingContext ctx = new PricingContext("VIP", "LOAN", new BigDecimal("10000"));
        BigDecimal discount = pricingPolicy.calculate(ctx);
        assertEquals(expectedDiscount(new BigDecimal("10000"), "0.05"), discount);
    }

    @Test
    void shouldNotApplyHighNetWorth_whenVipFundButAmountBelow50000() {
        PricingContext ctx = new PricingContext("VIP", "FUND", new BigDecimal("40000"));
        BigDecimal discount = pricingPolicy.calculate(ctx);
        assertEquals(expectedDiscount(new BigDecimal("40000"), "0.10"), discount);
    }

    @Test
    void shouldPrioritizeHighNetWorthOverVipHighAmount() {
        PricingContext ctx = new PricingContext("VIP", "FUND", new BigDecimal("80000"));
        BigDecimal discount = pricingPolicy.calculate(ctx);
        assertEquals(expectedDiscount(new BigDecimal("80000"), "0.08"), discount);
    }
}
