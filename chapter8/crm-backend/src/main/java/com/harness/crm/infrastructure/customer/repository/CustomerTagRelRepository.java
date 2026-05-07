package com.harness.crm.infrastructure.customer.repository;

import com.harness.crm.domain.customer.entity.CustomerTagRelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerTagRelRepository extends JpaRepository<CustomerTagRelEntity, Long> {

    long countByTagId(Long tagId);

    void deleteByCustomerId(Long customerId);

    List<CustomerTagRelEntity> findByCustomerId(Long customerId);
}
