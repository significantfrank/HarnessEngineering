package com.harness.customer.domain.service;

import com.harness.customer.domain.entity.Customer;
import com.harness.customer.domain.gateway.CustomerGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerDomainService {

    private final CustomerGateway customerGateway;

    public void checkIdNumberUnique(String idNumber) {
        if (customerGateway.existsByIdNumber(idNumber)) {
            throw new IllegalArgumentException("Customer with idNumber " + idNumber + " already exists");
        }
    }

    public void checkIdNumberUniqueForUpdate(String idNumber, Long excludeId) {
        customerGateway.findById(excludeId).ifPresent(existing -> {
            if (!existing.getIdNumber().equals(idNumber) && customerGateway.existsByIdNumber(idNumber)) {
                throw new IllegalArgumentException("Customer with idNumber " + idNumber + " already exists");
            }
        });
    }
}
