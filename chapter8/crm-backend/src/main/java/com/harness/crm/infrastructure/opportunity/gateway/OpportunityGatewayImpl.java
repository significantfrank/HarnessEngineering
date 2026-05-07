package com.harness.crm.infrastructure.opportunity.gateway;

import com.harness.crm.domain.opportunity.entity.OpportunityEntity;
import com.harness.crm.domain.opportunity.enums.OppStage;
import com.harness.crm.domain.opportunity.gateway.OpportunityGatewayI;
import com.harness.crm.infrastructure.opportunity.repository.OpportunityRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OpportunityGatewayImpl implements OpportunityGatewayI {

    private final OpportunityRepository opportunityRepository;

    @Override
    public OpportunityEntity save(OpportunityEntity entity) {
        return opportunityRepository.save(entity);
    }

    @Override
    public Optional<OpportunityEntity> findById(Long id) {
        return opportunityRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        opportunityRepository.deleteById(id);
    }

    @Override
    public Page<OpportunityEntity> findByConditions(String title, OppStage stage, Long customerId, String ownerName, Pageable pageable) {
        Specification<OpportunityEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (title != null && !title.isBlank()) {
                predicates.add(cb.like(root.get("title"), "%" + title + "%"));
            }
            if (stage != null) {
                predicates.add(cb.equal(root.get("stage"), stage));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customerId"), customerId));
            }
            if (ownerName != null && !ownerName.isBlank()) {
                predicates.add(cb.like(root.get("ownerName"), "%" + ownerName + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return opportunityRepository.findAll(spec, pageable);
    }

    @Override
    public List<OpportunityEntity> findAllForKanban() {
        return opportunityRepository.findAll();
    }
}
