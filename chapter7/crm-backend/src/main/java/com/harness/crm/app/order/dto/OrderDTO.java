package com.harness.crm.app.order.dto;

import com.harness.crm.domain.order.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;

    private String orderNo;

    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    @NotNull(message = "机会ID不能为空")
    private Long opportunityId;

    @NotNull(message = "订单金额不能为空")
    private BigDecimal totalAmount;

    private OrderStatus status;

    private String ownerName;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
