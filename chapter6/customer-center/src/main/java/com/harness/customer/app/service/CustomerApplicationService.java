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

    public List<Customer> listCustomers(String name, String phone, AccountStatus accountStatus) {
        return customerGateway.findByCondition(name, phone, accountStatus);
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer customer) {
        Customer existing = getCustomer(id);
        customerDomainService.checkIdNumberUniqueForUpdate(customer.getIdNumber(), id);
        existing.setName(customer.getName());
        existing.setIdType(customer.getIdType());
        existing.setIdNumber(customer.getIdNumber());
        existing.setPhone(customer.getPhone());
        existing.setEmail(customer.getEmail());
        existing.setGender(customer.getGender());
        existing.setBirthday(customer.getBirthday());
        existing.setIdPhotoUrl(customer.getIdPhotoUrl());
        existing.setRiskProfile(customer.getRiskProfile());
        existing.setOccupation(customer.getOccupation());
        existing.setIncomeRange(customer.getIncomeRange());
        existing.setAum(customer.getAum());
        existing.setAvailableBalance(customer.getAvailableBalance());
        existing.setTotalReturn(customer.getTotalReturn());
        existing.setHoldingProducts(customer.getHoldingProducts());
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
