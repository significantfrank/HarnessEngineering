package com.harness.crm.domain.customer.service;

import com.harness.crm.domain.customer.entity.CustomerEntity;
import com.harness.crm.domain.customer.enums.CustomerLevel;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
import com.harness.crm.domain.customer.gateway.CustomerGatewayI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerDomainService {

    private final CustomerGatewayI customerGateway;

    public CustomerEntity create(CustomerEntity entity) {
        entity.prePersist();
        return customerGateway.save(entity);
    }

    public CustomerEntity update(CustomerEntity entity) {
        entity.preUpdate();
        return customerGateway.save(entity);
    }

    public Optional<CustomerEntity> findById(Long id) {
        return customerGateway.findById(id);
    }

    public void deleteById(Long id) {
        customerGateway.deleteById(id);
    }

    public Page<CustomerEntity> findByConditions(String name, CustomerStatus status, CustomerSource source, CustomerLevel level, Pageable pageable) {
        return customerGateway.findByConditions(name, status, source, level, pageable);
    }
}
