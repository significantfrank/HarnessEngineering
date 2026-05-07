package com.harness.crm.domain.customer.gateway;

import com.harness.crm.domain.customer.entity.CustomerTagRelEntity;

import java.util.List;

public interface CustomerTagGatewayI {

    CustomerTagRelEntity save(CustomerTagRelEntity entity);

    void deleteByCustomerId(Long customerId);

    List<CustomerTagRelEntity> findByCustomerId(Long customerId);

    long countByTagId(Long tagId);
}
