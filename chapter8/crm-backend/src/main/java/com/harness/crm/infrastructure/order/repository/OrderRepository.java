package com.harness.crm.infrastructure.order.repository;

import com.harness.crm.domain.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {

    /**
     * 查询指定日期前缀的最大订单号
     */
    @Query("SELECT MAX(o.orderNo) FROM OrderEntity o WHERE o.orderNo LIKE CONCAT(:prefix, '%')")
    String findMaxOrderNoByPrefix(@Param("prefix") String prefix);
}
