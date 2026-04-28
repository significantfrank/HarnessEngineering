package com.harness.crm.domain.order.service;

import com.harness.crm.domain.order.service.compliance.ComplianceContext;
import com.harness.crm.domain.order.service.compliance.ComplianceRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 合规领域服务：遍历所有适用的合规规则，任一不通过即拒绝
 * 新增合规规则只需实现 ComplianceRule 并标注 @Component + @Order
 */
@Slf4j
@Service
public class CompliancePolicy {

    private final List<ComplianceRule> rules;

    public CompliancePolicy(List<ComplianceRule> rules) {
        this.rules = rules;
    }

    /**
     * 执行合规校验，遍历所有适用规则，任一不通过则抛出异常
     */
    public void check(ComplianceContext context) {
        rules.stream()
                .filter(rule -> rule.matches(context))
                .forEach(rule -> {
                    log.info("执行合规规则: {}", rule.getClass().getSimpleName());
                    rule.check(context);
                });
    }
}
