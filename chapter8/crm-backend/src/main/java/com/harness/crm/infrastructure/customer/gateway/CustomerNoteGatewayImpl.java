package com.harness.crm.infrastructure.customer.gateway;

import com.harness.crm.domain.customer.entity.CustomerNoteEntity;
import com.harness.crm.domain.customer.gateway.CustomerNoteGatewayI;
import com.harness.crm.infrastructure.customer.repository.CustomerNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerNoteGatewayImpl implements CustomerNoteGatewayI {

    private final CustomerNoteRepository customerNoteRepository;

    @Override
    public CustomerNoteEntity save(CustomerNoteEntity entity) {
        return customerNoteRepository.save(entity);
    }

    @Override
    public Page<CustomerNoteEntity> findByCustomerId(Long customerId, Pageable pageable) {
        return customerNoteRepository.findByCustomerIdOrderByCreateTimeDesc(customerId, pageable);
    }
}
