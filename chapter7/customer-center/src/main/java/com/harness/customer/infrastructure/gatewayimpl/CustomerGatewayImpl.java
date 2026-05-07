package com.harness.customer.infrastructure.gatewayimpl;

import com.harness.customer.domain.entity.AccountStatus;
import com.harness.customer.domain.entity.Customer;
import com.harness.customer.domain.gateway.CustomerGateway;
import com.harness.customer.infrastructure.repository.CustomerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerGatewayImpl implements CustomerGateway {

    private final CustomerJpaRepository customerJpaRepository;

    @Override
    public Customer save(Customer customer) {
        return customerJpaRepository.save(customer);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return customerJpaRepository.findById(id);
    }

    @Override
    public List<Customer> findAll() {
        return customerJpaRepository.findAll();
    }

    @Override
    public List<Customer> findByCondition(String name, String phone, AccountStatus accountStatus) {
        return customerJpaRepository.findByCondition(name, phone, accountStatus);
    }

    @Override
    public void deleteById(Long id) {
        customerJpaRepository.deleteById(id);
    }

    @Override
    public Optional<Customer> findByIdNumber(String idNumber) {
        return customerJpaRepository.findByIdNumber(idNumber);
    }

    @Override
    public boolean existsByIdNumber(String idNumber) {
        return customerJpaRepository.existsByIdNumber(idNumber);
    }
}
