package com.harness.crm.app.order;

import com.harness.crm.app.order.dto.OrderDTO;
import com.harness.crm.domain.order.entity.OrderEntity;
import com.harness.crm.domain.order.enums.OrderStatus;
import com.harness.crm.domain.order.gateway.OrderGatewayI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderGatewayI orderGateway;

    /**
     * 创建订单，自动生成订单号
     */
    public OrderDTO create(OrderDTO dto) {
        OrderEntity entity = toEntity(dto);
        entity.generateOrderNo(orderGateway);
        entity.prePersist();
        OrderEntity saved = orderGateway.save(entity);
        return toDTO(saved);
    }

    /**
     * 更新订单，终态不可更新，orderNo不可修改
     */
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
