package com.harness.customer.domain.gateway;

import com.harness.customer.domain.entity.AccountStatus;
import com.harness.customer.domain.entity.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerGateway {

    Customer save(Customer customer);

    Optional<Customer> findById(Long id);

    List<Customer> findAll();

    List<Customer> findByCondition(String name, String phone, AccountStatus accountStatus);

    void deleteById(Long id);

    boolean existsByIdNumber(String idNumber);
}
