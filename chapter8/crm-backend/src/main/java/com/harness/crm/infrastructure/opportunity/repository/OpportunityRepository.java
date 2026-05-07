package com.harness.crm.infrastructure.opportunity.repository;

import com.harness.crm.domain.opportunity.entity.OpportunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OpportunityRepository extends JpaRepository<OpportunityEntity, Long>, JpaSpecificationExecutor<OpportunityEntity> {
}
