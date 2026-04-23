package com.harness.crm.infrastructure.customer.gateway;

import com.harness.crm.domain.customer.entity.CustomerTagRelEntity;
import com.harness.crm.domain.customer.gateway.CustomerTagGatewayI;
import com.harness.crm.infrastructure.customer.repository.CustomerTagRelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomerTagGatewayImpl implements CustomerTagGatewayI {

    private final CustomerTagRelRepository customerTagRelRepository;

    @Override
    public CustomerTagRelEntity save(CustomerTagRelEntity entity) {
        return customerTagRelRepository.save(entity);
    }

    @Override
    public void deleteByCustomerId(Long customerId) {
        customerTagRelRepository.deleteByCustomerId(customerId);
    }

    @Override
    public List<CustomerTagRelEntity> findByCustomerId(Long customerId) {
        return customerTagRelRepository.findByCustomerId(customerId);
    }

    @Override
    public long countByTagId(Long tagId) {
        return customerTagRelRepository.countByTagId(tagId);
    }
}
