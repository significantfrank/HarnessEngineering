package com.harness.crm.domain.customer.gateway;

import com.harness.crm.domain.customer.data.CustomerCenterData;

import java.util.Optional;

public interface CustomerCenterGatewayI {

    void createOrSync(String name, String phone, String email, String idType, String idNumber);

    void update(String idNumber, String name, String phone, String email, String idType);

    Optional<CustomerCenterData> findByIdNumber(String idNumber);

    boolean isAvailable();
}
