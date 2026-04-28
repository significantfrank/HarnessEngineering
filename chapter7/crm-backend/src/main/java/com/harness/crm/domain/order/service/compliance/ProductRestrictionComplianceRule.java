package com.harness.crm.domain.order.service.compliance;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 产品限制合规规则：NORMAL/POTENTIAL 等级客户禁止购买 FUND/BOND 类产品
 */
@Component
@Order(2)
public class ProductRestrictionComplianceRule implements ComplianceRule {

    @Override
    public boolean matches(ComplianceContext context) {
        String productType = context.getProductType();
        return "FUND".equals(productType) || "BOND".equals(productType);
    }

    @Override
    public void check(ComplianceContext context) {
        String level = context.getCustomerLevel();
        if ("NORMAL".equals(level) || "POTENTIAL".equals(level)) {
            throw new RuntimeException("客户等级不足以购买理财产品，拒绝下单");
        }
    }
}
