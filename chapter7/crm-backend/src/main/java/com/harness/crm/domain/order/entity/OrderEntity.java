package com.harness.crm.domain.order.entity;

import com.harness.crm.domain.order.enums.OrderStatus;
import com.harness.crm.domain.order.gateway.OrderGatewayI;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "`order`")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String orderNo;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long opportunityId;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(length = 100)
    private String ownerName;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(nullable = false)
    private LocalDateTime updateTime;

    /**
     * 创建前初始化默认值
     */
    public void prePersist() {
        if (status == null) {
            status = OrderStatus.PENDING;
        }
        LocalDateTime now = LocalDateTime.now();
        if (createTime == null) {
            createTime = now;
        }
        updateTime = now;
    }

    /**
     * 更新前刷新时间
     */
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

    /**
     * 是否处于终态
     */
    public boolean isTerminal() {
        return status != null && status.isTerminal();
    }

    /**
     * 生成订单号：ORD-yyyyMMdd-NNN
     */
    public void generateOrderNo(OrderGatewayI orderGateway) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePrefix = "ORD-" + LocalDate.now().format(dateFormat) + "-";
        String maxOrderNo = orderGateway.findMaxOrderNoByDate(datePrefix);
        int nextSeq = 1;
        if (maxOrderNo != null) {
            String seqPart = maxOrderNo.substring(datePrefix.length());
            nextSeq = Integer.parseInt(seqPart) + 1;
        }
        this.orderNo = datePrefix + String.format("%03d", nextSeq);
    }
}
