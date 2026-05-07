package com.harness.customer.domain.service;

import com.harness.customer.domain.entity.Customer;
import com.harness.customer.domain.entity.FreezeReason;
import com.harness.customer.domain.gateway.CustomerGateway;
import com.harness.customer.domain.gateway.JudicialAuthorizationGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerDomainService {

    private final CustomerGateway customerGateway;
    private final JudicialAuthorizationGateway judicialAuthorizationGateway;

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

    public void validateUnfreezeAuthorization(Customer customer, String authorizationDocument) {
        if (customer.getFreezeReason() == FreezeReason.JUDICIAL) {
            if (authorizationDocument == null || authorizationDocument.isBlank()) {
                throw new IllegalArgumentException("Judicial freeze requires unfreeze authorization document");
            }
            if (!judicialAuthorizationGateway.verify(authorizationDocument)) {
                throw new IllegalArgumentException("Invalid judicial unfreeze authorization");
            }
        }
    }
}
