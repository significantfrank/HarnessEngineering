package com.harness.crm.app.customer;

import com.harness.crm.app.customer.dto.CustomerDTO;
import com.harness.crm.domain.customer.entity.CustomerEntity;
import com.harness.crm.domain.customer.enums.CustomerLevel;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
import com.harness.crm.domain.customer.service.CustomerDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerDomainService customerDomainService;

    public CustomerDTO create(CustomerDTO dto) {
        CustomerEntity entity = toEntity(dto);
        CustomerEntity saved = customerDomainService.create(entity);
        return toDTO(saved);
    }

    public CustomerDTO update(Long id, CustomerDTO dto) {
        CustomerEntity existing = customerDomainService.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        updateEntity(existing, dto);
        CustomerEntity saved = customerDomainService.update(existing);
        return toDTO(saved);
    }

    public CustomerDTO findById(Long id) {
        CustomerEntity entity = customerDomainService.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        return toDTO(entity);
    }

    public Page<CustomerDTO> findByConditions(String name, CustomerStatus status, CustomerSource source, CustomerLevel level, int page, int size) {
        Page<CustomerEntity> entityPage = customerDomainService.findByConditions(name, status, source, level, PageRequest.of(page, size));
        return entityPage.map(this::toDTO);
    }

    public void deleteById(Long id) {
        customerDomainService.deleteById(id);
    }

    private CustomerEntity toEntity(CustomerDTO dto) {
        return CustomerEntity.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .company(dto.getCompany())
                .address(dto.getAddress())
                .source(dto.getSource())
                .level(dto.getLevel())
                .industry(dto.getIndustry())
                .website(dto.getWebsite())
                .contactPerson(dto.getContactPerson())
                .lastFollowUp(dto.getLastFollowUp())
                .status(dto.getStatus())
                .remark(dto.getRemark())
                .build();
    }

    private void updateEntity(CustomerEntity entity, CustomerDTO dto) {
        entity.setName(dto.getName());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setCompany(dto.getCompany());
        entity.setAddress(dto.getAddress());
        entity.setSource(dto.getSource());
        entity.setLevel(dto.getLevel());
        entity.setIndustry(dto.getIndustry());
        entity.setWebsite(dto.getWebsite());
        entity.setContactPerson(dto.getContactPerson());
        entity.setLastFollowUp(dto.getLastFollowUp());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
    }

    private CustomerDTO toDTO(CustomerEntity entity) {
        return CustomerDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .company(entity.getCompany())
                .address(entity.getAddress())
                .source(entity.getSource())
                .level(entity.getLevel())
                .industry(entity.getIndustry())
                .website(entity.getWebsite())
                .contactPerson(entity.getContactPerson())
                .lastFollowUp(entity.getLastFollowUp())
                .status(entity.getStatus())
                .remark(entity.getRemark())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
