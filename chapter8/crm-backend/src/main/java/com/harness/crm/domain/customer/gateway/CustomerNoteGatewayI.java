package com.harness.crm.domain.customer.gateway;

import com.harness.crm.domain.customer.entity.CustomerNoteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerNoteGatewayI {

    CustomerNoteEntity save(CustomerNoteEntity entity);

    Page<CustomerNoteEntity> findByCustomerId(Long customerId, Pageable pageable);
}
