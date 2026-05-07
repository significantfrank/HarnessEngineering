package com.harness.crm.domain.lead.gateway;

import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.lead.entity.LeadEntity;
import com.harness.crm.domain.lead.enums.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LeadGatewayI {

    LeadEntity save(LeadEntity entity);

    Optional<LeadEntity> findById(Long id);

    void deleteById(Long id);

    Page<LeadEntity> findByConditions(String name, LeadStatus status, CustomerSource source, Pageable pageable);
}
