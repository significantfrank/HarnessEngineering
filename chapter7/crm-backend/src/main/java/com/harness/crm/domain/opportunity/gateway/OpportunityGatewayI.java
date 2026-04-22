package com.harness.crm.domain.opportunity.gateway;

import com.harness.crm.domain.opportunity.entity.OpportunityEntity;
import com.harness.crm.domain.opportunity.enums.OppStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OpportunityGatewayI {

    OpportunityEntity save(OpportunityEntity entity);

    Optional<OpportunityEntity> findById(Long id);

    void deleteById(Long id);

    Page<OpportunityEntity> findByConditions(String title, OppStage stage, Long customerId, String ownerName, Pageable pageable);

    /**
     * 查询全部机会，用于看板全量加载
     */
    List<OpportunityEntity> findAllForKanban();
}
