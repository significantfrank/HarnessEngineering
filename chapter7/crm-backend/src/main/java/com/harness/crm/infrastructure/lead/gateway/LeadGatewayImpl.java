package com.harness.crm.infrastructure.lead.gateway;

import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.lead.entity.LeadEntity;
import com.harness.crm.domain.lead.enums.LeadStatus;
import com.harness.crm.domain.lead.gateway.LeadGatewayI;
import com.harness.crm.infrastructure.lead.repository.LeadRepository;
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
public class LeadGatewayImpl implements LeadGatewayI {

    private final LeadRepository leadRepository;

    @Override
    public LeadEntity save(LeadEntity entity) {
        return leadRepository.save(entity);
    }

    @Override
    public Optional<LeadEntity> findById(Long id) {
        return leadRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        leadRepository.deleteById(id);
    }

    @Override
    public Page<LeadEntity> findByConditions(String name, LeadStatus status, CustomerSource source, Pageable pageable) {
        Specification<LeadEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (source != null) {
                predicates.add(cb.equal(root.get("source"), source));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return leadRepository.findAll(spec, pageable);
    }
}
