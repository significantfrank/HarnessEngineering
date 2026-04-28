package com.harness.crm.domain.order.service.compliance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 合规校验上下文，封装合规所需的客户与产品信息
 */
@Getter
@AllArgsConstructor
public class ComplianceContext {

    private final String customerLevel;

    private final String customerStatus;

    private final String productType;
}
