package com.harness.crm.infrastructure.lead.repository;

import com.harness.crm.domain.lead.entity.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LeadRepository extends JpaRepository<LeadEntity, Long>, JpaSpecificationExecutor<LeadEntity> {
}
