package com.harness.crm.domain.customer.gateway;

import com.harness.crm.domain.customer.entity.CustomerEntity;
import com.harness.crm.domain.customer.enums.CustomerLevel;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomerGatewayI {

    CustomerEntity save(CustomerEntity entity);

    Optional<CustomerEntity> findById(Long id);

    void deleteById(Long id);

    Page<CustomerEntity> findByConditions(String name, CustomerStatus status, CustomerSource source, CustomerLevel level, Pageable pageable);
}
