package com.harness.crm.app.order;

import com.harness.crm.app.order.dto.OrderDTO;
import com.harness.crm.domain.customer.entity.CustomerEntity;
import com.harness.crm.domain.customer.gateway.CustomerGatewayI;
import com.harness.crm.domain.customer.gateway.CustomerNotifyGatewayI;
import com.harness.crm.domain.order.entity.OrderEntity;
import com.harness.crm.domain.order.enums.OrderStatus;
import com.harness.crm.domain.order.gateway.OrderGatewayI;
import com.harness.crm.domain.order.service.CompliancePolicy;
import com.harness.crm.domain.order.service.pricing.PricingContext;
import com.harness.crm.domain.order.service.PricingPolicy;
import com.harness.crm.domain.order.service.compliance.ComplianceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderGatewayI orderGateway;
    private final CustomerGatewayI customerGateway;
    private final PricingPolicy pricingPolicy;
    private final CompliancePolicy compliancePolicy;
    private final RiskSystemService riskSystemService;
    private final AuditLogService auditLogService;
    private final CustomerNotifyGatewayI customerNotifyGateway;

    /** 创建订单，自动生成订单号 */
    public OrderDTO create(OrderDTO dto) {
        OrderEntity entity = toEntity(dto);
        entity.generateOrderNo(orderGateway);
        entity.prePersist();
        OrderEntity saved = orderGateway.save(entity);
        return toDTO(saved);
    }

    /** 更新订单，终态不可更新，orderNo不可修改 */
    public OrderDTO update(Long id, OrderDTO dto) {
        OrderEntity existing = orderGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        if (existing.isTerminal()) {
            throw new RuntimeException("订单已处于终态，不可更新: " + existing.getStatus());
        }
        updateEntity(existing, dto);
        existing.preUpdate();
        OrderEntity saved = orderGateway.save(existing);
        return toDTO(saved);
    }

    public OrderDTO findById(Long id) {
        OrderEntity entity = orderGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        return toDTO(entity);
    }

    public Page<OrderDTO> findByConditions(String orderNo, OrderStatus status, Long customerId, Long opportunityId, int page, int size) {
        Page<OrderEntity> entityPage = orderGateway.findByConditions(orderNo, status, customerId, opportunityId, PageRequest.of(page, size));
        return entityPage.map(this::toDTO);
    }

    public void deleteById(Long id) {
        orderGateway.deleteById(id);
    }

    /** 订单处理主流程：校验→合规→折扣→风控→落库→审计→通知 */
    public OrderDTO processOrder(OrderDTO dto) {
        validateProcessRequest(dto);

        CustomerEntity customer = findCustomerForCompliance(dto.getCustomerId());
        checkCompliance(customer, dto.getProductType());

        BigDecimal discount = calculateDiscount(dto);
        OrderEntity order = buildProcessOrderEntity(dto, discount);
        order.generateOrderNo(orderGateway);
        order.prePersist();

        riskSystemService.checkRisk(order.getOrderNo());
        OrderEntity saved = orderGateway.save(order);

        auditLogService.log(saved.getOrderNo(), "FINANCIAL_TRANSACTION_CREATED");
        customerNotifyGateway.sendConfirmation(customer, order);

        return toProcessOrderDTO(saved, dto);
    }

    /** 参数校验 */
    private void validateProcessRequest(OrderDTO dto) {
        if (dto == null || dto.getCustomerId() == null || dto.getTotalAmount() == null) {
            throw new IllegalArgumentException("请求参数缺失");
        }
    }

    /** 查询客户信息用于合规校验 */
    private CustomerEntity findCustomerForCompliance(Long customerId) {
        return customerGateway.findById(customerId)
                .orElseThrow(() -> new RuntimeException("客户不存在: " + customerId));
    }

    /** 合规校验 */
    private void checkCompliance(CustomerEntity customer, String productType) {
        ComplianceContext context = new ComplianceContext(
                customer.getLevel().name(),
                customer.getStatus().name(),
                productType
        );
        compliancePolicy.check(context);
    }

    /** 折扣计算 */
    private BigDecimal calculateDiscount(OrderDTO dto) {
        PricingContext context = new PricingContext(
                dto.getCustomerLevel(),
                dto.getProductType(),
                dto.getTotalAmount()
        );
        return pricingPolicy.calculate(context);
    }

    /** 构建订单实体（扣减折扣后金额） */
    private OrderEntity buildProcessOrderEntity(OrderDTO dto, BigDecimal discount) {
        return OrderEntity.builder()
                .customerId(dto.getCustomerId())
                .opportunityId(dto.getOpportunityId())
                .totalAmount(dto.getTotalAmount().subtract(discount))
                .status(OrderStatus.PENDING)
                .ownerName(dto.getOwnerName())
                .remark(dto.getRemark())
                .build();
    }

    /** 将落库实体转为DTO，并回填请求中的非持久化字段 */
    private OrderDTO toProcessOrderDTO(OrderEntity entity, OrderDTO original) {
        OrderDTO dto = toDTO(entity);
        dto.setEmail(original.getEmail());
        dto.setCustomerLevel(original.getCustomerLevel());
        dto.setProductType(original.getProductType());
        return dto;
    }

    private OrderEntity toEntity(OrderDTO dto) {
        return OrderEntity.builder()
                .customerId(dto.getCustomerId())
                .opportunityId(dto.getOpportunityId())
                .totalAmount(dto.getTotalAmount())
                .status(dto.getStatus())
                .ownerName(dto.getOwnerName())
                .remark(dto.getRemark())
                .build();
    }

    private void updateEntity(OrderEntity entity, OrderDTO dto) {
        entity.setCustomerId(dto.getCustomerId());
        entity.setOpportunityId(dto.getOpportunityId());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setStatus(dto.getStatus());
        entity.setOwnerName(dto.getOwnerName());
        entity.setRemark(dto.getRemark());
    }

    private OrderDTO toDTO(OrderEntity entity) {
        return OrderDTO.builder()
                .id(entity.getId())
                .orderNo(entity.getOrderNo())
                .customerId(entity.getCustomerId())
                .opportunityId(entity.getOpportunityId())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus())
                .ownerName(entity.getOwnerName())
                .remark(entity.getRemark())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
