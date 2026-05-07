package com.harness.crm.domain.order.gateway;

import com.harness.crm.domain.order.entity.OrderEntity;
import com.harness.crm.domain.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderGatewayI {

    OrderEntity save(OrderEntity entity);

    Optional<OrderEntity> findById(Long id);

    void deleteById(Long id);

    Page<OrderEntity> findByConditions(String orderNo, OrderStatus status, Long customerId, Long opportunityId, Pageable pageable);

    /**
     * 查询指定日期的最大订单号
     */
    String findMaxOrderNoByDate(String datePrefix);
}
