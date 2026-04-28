package com.harness.crm.domain.order.service.compliance;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 客户状态合规规则：INACTIVE/LOST 状态客户禁止下单
 */
@Component
@Order(1)
public class CustomerStatusComplianceRule implements ComplianceRule {

    @Override
    public boolean matches(ComplianceContext context) {
        return true;
    }

    @Override
    public void check(ComplianceContext context) {
        String status = context.getCustomerStatus();
        if ("INACTIVE".equals(status) || "LOST".equals(status)) {
            throw new RuntimeException("客户状态异常，拒绝下单");
        }
    }
}
