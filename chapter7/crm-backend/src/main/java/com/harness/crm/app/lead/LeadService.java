package com.harness.crm.app.lead;

import com.harness.crm.app.lead.dto.LeadConvertDTO;
import com.harness.crm.app.lead.dto.LeadDTO;
import com.harness.crm.domain.customer.entity.CustomerEntity;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
import com.harness.crm.domain.customer.gateway.CustomerGatewayI;
import com.harness.crm.domain.lead.entity.LeadEntity;
import com.harness.crm.domain.lead.enums.LeadStatus;
import com.harness.crm.domain.lead.gateway.LeadGatewayI;
import com.harness.crm.domain.opportunity.entity.OpportunityEntity;
import com.harness.crm.domain.opportunity.enums.OppStage;
import com.harness.crm.domain.opportunity.gateway.OpportunityGatewayI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadGatewayI leadGateway;
    private final CustomerGatewayI customerGateway;
    private final OpportunityGatewayI opportunityGateway;

    public LeadDTO create(LeadDTO dto) {
        LeadEntity entity = toEntity(dto);
        entity.prePersist();
        LeadEntity saved = leadGateway.save(entity);
        return toDTO(saved);
    }

    public LeadDTO update(Long id, LeadDTO dto) {
        LeadEntity existing = leadGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found: " + id));
        if (existing.isTerminal()) {
            throw new RuntimeException("线索已处于终态，不可更新: " + existing.getStatus());
        }
        updateEntity(existing, dto);
        existing.preUpdate();
        LeadEntity saved = leadGateway.save(existing);
        return toDTO(saved);
    }

    public LeadDTO findById(Long id) {
        LeadEntity entity = leadGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found: " + id));
        return toDTO(entity);
    }

    public Page<LeadDTO> findByConditions(String name, LeadStatus status, CustomerSource source, int page, int size) {
        Page<LeadEntity> entityPage = leadGateway.findByConditions(name, status, source, PageRequest.of(page, size));
        return entityPage.map(this::toDTO);
    }

    public void deleteById(Long id) {
        leadGateway.deleteById(id);
    }

    /**
     * 线索转化：原子创建客户+机会，标记线索已转化
     */
    @Transactional
    public Map<String, Object> convert(Long id, LeadConvertDTO convertDTO) {
        LeadEntity lead = leadGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found: " + id));

        if (lead.isTerminal()) {
            throw new RuntimeException("线索已处于终态，不可转化: " + lead.getStatus());
        }

        CustomerEntity customer = CustomerEntity.builder()
                .name(lead.getName())
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .company(lead.getCompany())
                .source(lead.getSource() != null ? lead.getSource() : CustomerSource.OTHER)
                .status(CustomerStatus.ACTIVE)
                .build();
        customer.prePersist();
        CustomerEntity savedCustomer = customerGateway.save(customer);

        OpportunityEntity opportunity = OpportunityEntity.builder()
                .title(convertDTO.getOpportunityTitle())
                .customerId(savedCustomer.getId())
                .amount(convertDTO.getAmount())
                .expectedCloseDate(convertDTO.getExpectedCloseDate())
                .leadId(lead.getId())
                .stage(OppStage.PROSPECTING)
                .build();
        opportunity.prePersist();
        OpportunityEntity savedOpp = opportunityGateway.save(opportunity);

        lead.setStatus(LeadStatus.CONVERTED);
        lead.setCustomerId(savedCustomer.getId());
        lead.preUpdate();
        leadGateway.save(lead);

        return Map.of(
                "customer", toCustomerDTO(savedCustomer),
                "opportunity", toOppDTO(savedOpp)
        );
    }

    private LeadEntity toEntity(LeadDTO dto) {
        return LeadEntity.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .company(dto.getCompany())
                .source(dto.getSource())
                .status(dto.getStatus())
                .ownerName(dto.getOwnerName())
                .remark(dto.getRemark())
                .build();
    }

    private void updateEntity(LeadEntity entity, LeadDTO dto) {
        entity.setName(dto.getName());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setCompany(dto.getCompany());
        entity.setSource(dto.getSource());
        entity.setStatus(dto.getStatus());
        entity.setOwnerName(dto.getOwnerName());
        entity.setRemark(dto.getRemark());
    }

    private LeadDTO toDTO(LeadEntity entity) {
        return LeadDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .company(entity.getCompany())
                .source(entity.getSource())
                .status(entity.getStatus())
                .customerId(entity.getCustomerId())
                .ownerName(entity.getOwnerName())
                .remark(entity.getRemark())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    private Map<String, Object> toCustomerDTO(CustomerEntity entity) {
        return Map.of(
                "id", entity.getId(),
                "name", entity.getName(),
                "phone", entity.getPhone() != null ? entity.getPhone() : "",
                "email", entity.getEmail() != null ? entity.getEmail() : "",
                "company", entity.getCompany() != null ? entity.getCompany() : "",
                "status", entity.getStatus().name()
        );
    }

    private Map<String, Object> toOppDTO(OpportunityEntity entity) {
        return Map.of(
                "id", entity.getId(),
                "title", entity.getTitle(),
                "customerId", entity.getCustomerId(),
                "stage", entity.getStage().name(),
                "amount", entity.getAmount() != null ? entity.getAmount() : java.math.BigDecimal.ZERO
        );
    }
}
