package com.harness.crm.infrastructure.customer.repository;

import com.harness.crm.domain.customer.entity.CustomerNoteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerNoteRepository extends JpaRepository<CustomerNoteEntity, Long> {

    Page<CustomerNoteEntity> findByCustomerIdOrderByCreateTimeDesc(Long customerId, Pageable pageable);
}
