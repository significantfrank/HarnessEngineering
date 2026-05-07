package com.harness.crm.app.lead;

import com.harness.crm.app.lead.dto.LeadConvertDTO;
import com.harness.crm.app.lead.dto.LeadDTO;
import com.harness.crm.domain.customer.entity.CustomerEntity;
import com.harness.crm.domain.customer.enums.CcSyncStatus;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
import com.harness.crm.domain.customer.gateway.CustomerCenterGatewayI;
import com.harness.crm.domain.customer.gateway.CustomerGatewayI;
import com.harness.crm.domain.lead.entity.LeadEntity;
import com.harness.crm.domain.lead.enums.LeadStatus;
import com.harness.crm.domain.lead.gateway.LeadGatewayI;
import com.harness.crm.domain.opportunity.entity.OpportunityEntity;
import com.harness.crm.domain.opportunity.enums.OppStage;
import com.harness.crm.domain.opportunity.gateway.OpportunityGatewayI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadGatewayI leadGateway;
    private final CustomerGatewayI customerGateway;
    private final OpportunityGatewayI opportunityGateway;
    private final CustomerCenterGatewayI customerCenterGateway;

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
     * 线索转化：事务1创建Customer(PENDING)+Opportunity+更新Lead → 事务外调center同步
     */
    @Transactional
    public Map<String, Object> convert(Long id, LeadConvertDTO convertDTO) {
        LeadEntity lead = leadGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found: " + id));

        if (lead.isTerminal()) {
            throw new RuntimeException("线索已处于终态，不可转化: " + lead.getStatus());
        }

        CustomerEntity customer = buildCustomerFromLead(lead, convertDTO);
        customer.prePersist();
        CustomerEntity savedCustomer = customerGateway.save(customer);

        OpportunityEntity opportunity = buildOpportunity(savedCustomer.getId(), lead.getId(), convertDTO);
        opportunity.prePersist();
        OpportunityEntity savedOpp = opportunityGateway.save(opportunity);

        LeadStatus originalStatus = lead.getStatus();
        Long originalCustomerId = lead.getCustomerId();
        lead.setStatus(LeadStatus.CONVERTED);
        lead.setCustomerId(savedCustomer.getId());
        lead.preUpdate();
        leadGateway.save(lead);

        return Map.of(
                "customerId", savedCustomer.getId(),
                "opportunityId", savedOpp.getId(),
                "customerName", savedCustomer.getName(),
                "customerPhone", savedCustomer.getPhone() != null ? savedCustomer.getPhone() : "",
                "customerEmail", savedCustomer.getEmail() != null ? savedCustomer.getEmail() : "",
                "customerIdType", savedCustomer.getIdType() != null ? savedCustomer.getIdType() : "",
                "customerIdNumber", savedCustomer.getIdNumber(),
                "originalStatus", originalStatus.name(),
                "originalCustomerId", originalCustomerId != null ? originalCustomerId : 0L
        );
    }

    /** 转化后同步center，失败则回滚 */
    public void syncAfterConvert(Long customerId, Long opportunityId, Long leadId,
                                  String originalStatus, Long originalCustomerId,
                                  String name, String phone, String email, String idType, String idNumber) {
        try {
            customerCenterGateway.createOrSync(name, phone, email, idType, idNumber);
            customerGateway.updateSyncStatus(customerId, CcSyncStatus.SYNCED);
        } catch (Exception e) {
            log.error("转化同步center失败，回滚: {}", e.getMessage());
            rollbackConvert(customerId, opportunityId, leadId, originalStatus, originalCustomerId);
            throw new RuntimeException("线索转化失败：主数据同步异常 - " + e.getMessage());
        }
    }

    /** 回滚转化：删除Customer+Opportunity，恢复Lead状态 */
    private void rollbackConvert(Long customerId, Long opportunityId, Long leadId,
                                  String originalStatus, Long originalCustomerId) {
        try {
            customerGateway.deleteById(customerId);
            opportunityGateway.deleteById(opportunityId);
            LeadEntity lead = leadGateway.findById(leadId).orElse(null);
            if (lead != null) {
                lead.setStatus(LeadStatus.valueOf(originalStatus));
                lead.setCustomerId(originalCustomerId != 0L ? originalCustomerId : null);
                lead.preUpdate();
                leadGateway.save(lead);
            }
        } catch (Exception e) {
            log.error("回滚转化失败: {}", e.getMessage());
        }
    }

    private CustomerEntity buildCustomerFromLead(LeadEntity lead, LeadConvertDTO convertDTO) {
        return CustomerEntity.builder()
                .name(lead.getName())
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .company(lead.getCompany())
                .source(lead.getSource() != null ? lead.getSource() : CustomerSource.OTHER)
                .status(CustomerStatus.ACTIVE)
                .idType(convertDTO.getIdType())
                .idNumber(convertDTO.getIdNumber())
                .build();
    }

    private OpportunityEntity buildOpportunity(Long customerId, Long leadId, LeadConvertDTO convertDTO) {
        return OpportunityEntity.builder()
                .title(convertDTO.getOpportunityTitle())
                .customerId(customerId)
                .amount(convertDTO.getAmount())
                .expectedCloseDate(convertDTO.getExpectedCloseDate())
                .leadId(leadId)
                .stage(OppStage.PROSPECTING)
                .build();
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
}
