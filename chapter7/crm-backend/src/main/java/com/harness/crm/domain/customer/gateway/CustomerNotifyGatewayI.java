package com.harness.crm.domain.customer.gateway;

import com.harness.crm.domain.customer.entity.CustomerEntity;
import com.harness.crm.domain.order.entity.OrderEntity;

public interface CustomerNotifyGatewayI {
    void sendConfirmation(CustomerEntity customer, OrderEntity order);
}
