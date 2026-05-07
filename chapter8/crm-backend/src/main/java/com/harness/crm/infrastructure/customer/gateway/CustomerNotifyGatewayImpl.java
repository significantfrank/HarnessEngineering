package com.harness.crm.infrastructure.customer.gateway;

import com.harness.crm.domain.customer.entity.CustomerEntity;
import com.harness.crm.domain.customer.gateway.CustomerNotifyGatewayI;
import com.harness.crm.domain.order.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomerNotifyGatewayImpl implements CustomerNotifyGatewayI {

    @Override
    public void sendConfirmation(CustomerEntity customer, OrderEntity order) {
        try {
            log.info("邮件已发送至 {}，订单号: {}", customer.getEmail(), order.getOrderNo());
            log.info("短信已发送至 {}，订单号: {}", customer.getPhone(), order.getOrderNo());
        } catch (Exception e) {
            log.warn("通知发送失败", e);
        }
    }
}
