package com.harness.crm.domain.order.service.compliance;

/**
 * 合规策略接口，新增合规规则只需实现此接口并标注 @Component + @Order
 */
public interface ComplianceRule {

    /**
     * 判断当前规则是否适用
     */
    boolean matches(ComplianceContext context);

    /**
     * 执行合规校验，不合规时抛出 RuntimeException
     */
    void check(ComplianceContext context);
}
