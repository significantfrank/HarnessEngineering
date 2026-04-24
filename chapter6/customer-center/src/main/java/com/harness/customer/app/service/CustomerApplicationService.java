package com.harness.customer.app.service;

import com.harness.customer.domain.entity.*;
import com.harness.customer.domain.gateway.CustomerGateway;
import com.harness.customer.domain.service.CustomerDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerApplicationService {

    private final CustomerGateway customerGateway;
    private final CustomerDomainService customerDomainService;

    @Transactional
    public Customer createCustomer(Customer customer) {
        customerDomainService.checkIdNumberUnique(customer.getIdNumber());
        customer.setAccountStatus(AccountStatus.NORMAL);
        customer.setMemberLevel(customer.getMemberLevel() != null ? customer.getMemberLevel() : MemberLevel.REGULAR);
        customer.setAuthLevel(customer.getAuthLevel() != null ? customer.getAuthLevel() : AuthLevel.L1);
        return customerGateway.save(customer);
    }

    public Customer getCustomer(Long id) {
        return customerGateway.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
    }

    public Customer getCustomerByIdNumber(String idNumber) {
        return customerGateway.findByIdNumber(idNumber)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found by idNumber: " + idNumber));
    }

    public List<Customer> listCustomers(String name, String phone, AccountStatus accountStatus) {
        return customerGateway.findByCondition(name, phone, accountStatus);
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer customer) {
        Customer existing = getCustomer(id);
        customerDomainService.checkIdNumberUniqueForUpdate(customer.getIdNumber(), id);
        if (customer.getName() != null) {
            existing.setName(customer.getName());
        }
        if (customer.getIdType() != null) {
            existing.setIdType(customer.getIdType());
        }
        if (customer.getIdNumber() != null) {
            existing.setIdNumber(customer.getIdNumber());
        }
        if (customer.getPhone() != null) {
            existing.setPhone(customer.getPhone());
        }
        if (customer.getEmail() != null) {
            existing.setEmail(customer.getEmail());
        }
        if (customer.getGender() != null) {
            existing.setGender(customer.getGender());
        }
        if (customer.getBirthday() != null) {
            existing.setBirthday(customer.getBirthday());
        }
        if (customer.getIdPhotoUrl() != null) {
            existing.setIdPhotoUrl(customer.getIdPhotoUrl());
        }
        if (customer.getRiskProfile() != null) {
            existing.setRiskProfile(customer.getRiskProfile());
        }
        if (customer.getOccupation() != null) {
            existing.setOccupation(customer.getOccupation());
        }
        if (customer.getIncomeRange() != null) {
            existing.setIncomeRange(customer.getIncomeRange());
        }
        if (customer.getAum() != null) {
            existing.setAum(customer.getAum());
        }
        if (customer.getAvailableBalance() != null) {
            existing.setAvailableBalance(customer.getAvailableBalance());
        }
        if (customer.getTotalReturn() != null) {
            existing.setTotalReturn(customer.getTotalReturn());
        }
        if (customer.getHoldingProducts() != null && !customer.getHoldingProducts().isEmpty()) {
            existing.setHoldingProducts(customer.getHoldingProducts());
        }
        return customerGateway.save(existing);
    }

    @Transactional
    public Customer freezeCustomer(Long id, FreezeReason reason) {
        Customer customer = getCustomer(id);
        customer.freezeAccount(reason);
        return customerGateway.save(customer);
    }

    @Transactional
    public Customer unfreezeCustomer(Long id, String reason, String authorizationDocument) {
        Customer customer = getCustomer(id);
        customerDomainService.validateUnfreezeAuthorization(customer, authorizationDocument);
        customer.unfreezeAccount(reason, authorizationDocument);
        return customerGateway.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        getCustomer(id);
        customerGateway.deleteById(id);
    }
}
