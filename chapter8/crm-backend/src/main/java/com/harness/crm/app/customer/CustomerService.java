package com.harness.crm.app.customer;

import com.harness.crm.app.customer.dto.Customer360DTO;
import com.harness.crm.app.customer.dto.CustomerDTO;
import com.harness.crm.app.customer.dto.CustomerNoteDTO;
import com.harness.crm.app.customer.dto.TagDTO;
import com.harness.crm.domain.customer.data.CustomerCenterData;
import com.harness.crm.domain.customer.entity.CustomerEntity;
import com.harness.crm.domain.customer.enums.CcSyncStatus;
import com.harness.crm.domain.customer.enums.CustomerLevel;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
import com.harness.crm.domain.customer.gateway.CustomerCenterGatewayI;
import com.harness.crm.domain.customer.gateway.CustomerGatewayI;
import com.harness.crm.domain.customer.gateway.CustomerNoteGatewayI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerGatewayI customerGateway;
    private final CustomerCenterGatewayI customerCenterGateway;
    private final CustomerNoteGatewayI customerNoteGateway;
    private final TagService tagService;

    /** 创建客户：事务1本地save(PENDING)+标签 → 事务外调center同步 → 更新状态或回滚 */
    @Transactional
    public CustomerDTO create(CustomerDTO dto) {
        CustomerEntity entity = toEntity(dto);
        entity.prePersist();
        CustomerEntity saved = customerGateway.save(entity);
        saveTags(saved.getId(), dto.getTagIds());
        return toDTO(saved);
    }

    /** 创建后同步center，成功更新SYNCED，失败回滚 */
    public void syncAfterCreate(Long customerId, String name, String phone, String email, String idType, String idNumber) {
        try {
            customerCenterGateway.createOrSync(name, phone, email, idType, idNumber);
            customerGateway.updateSyncStatus(customerId, CcSyncStatus.SYNCED);
        } catch (Exception e) {
            log.error("创建客户center同步失败，回滚本地记录: {}", e.getMessage());
            tagService.deleteCustomerTagRelations(customerId);
            customerGateway.deleteById(customerId);
            throw new RuntimeException("客户创建失败：主数据同步异常 - " + e.getMessage());
        }
    }

    /** 更新客户：事务1本地update+标签 → 事务外调center同步 → 更新状态或标记FAILED */
    @Transactional
    public CustomerDTO update(Long id, CustomerDTO dto) {
        CustomerEntity existing = customerGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        updateEntity(existing, dto);
        existing.preUpdate();
        CustomerEntity saved = customerGateway.save(existing);
        replaceTags(saved.getId(), dto.getTagIds());
        return toDTO(saved);
    }

    /** 更新后同步center，成功更新SYNCED，失败标记FAILED */
    public void syncAfterUpdate(Long customerId, String idNumber, String name, String phone, String email, String idType) {
        try {
            customerCenterGateway.update(idNumber, name, phone, email, idType);
            customerGateway.updateSyncStatus(customerId, CcSyncStatus.SYNCED);
        } catch (Exception e) {
            log.warn("更新客户center同步失败，标记FAILED: {}", e.getMessage());
            customerGateway.updateSyncStatus(customerId, CcSyncStatus.FAILED);
        }
    }

    public CustomerDTO findById(Long id) {
        CustomerEntity entity = customerGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        return toDTO(entity);
    }

    public Page<CustomerDTO> findByConditions(String name, CustomerStatus status, CustomerSource source, CustomerLevel level, List<Long> tagIds, int page, int size) {
        Page<CustomerEntity> entityPage = customerGateway.findByConditions(name, status, source, level, tagIds, PageRequest.of(page, size));
        return entityPage.map(this::toDTO);
    }

    public void deleteById(Long id) {
        customerGateway.deleteById(id);
    }

    /** 客户360视图：组装center主数据 + CRM行为数据 */
    public Customer360DTO find360ById(Long id) {
        CustomerEntity entity = customerGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + id));
        return build360DTO(entity);
    }

    private Customer360DTO build360DTO(CustomerEntity entity) {
        CustomerCenterData centerData = fetchCenterData(entity.getIdNumber());
        boolean degraded = centerData == null;

        List<TagDTO> tags = tagService.findTagsByCustomerId(entity.getId());
        List<CustomerNoteDTO> notes = customerNoteGateway.findByCustomerId(entity.getId(), PageRequest.of(0, 100))
                .map(this::toNoteDTO).toList();

        Customer360DTO dto = Customer360DTO.builder()
                .id(entity.getId())
                .name(centerData != null ? centerData.getName() : entity.getName())
                .phone(centerData != null ? centerData.getPhone() : entity.getPhone())
                .email(centerData != null ? centerData.getEmail() : entity.getEmail())
                .idType(centerData != null ? centerData.getIdType() : entity.getIdType())
                .idNumber(entity.getIdNumber())
                .ccSyncStatus(entity.getCcSyncStatus())
                .source(entity.getSource()).level(entity.getLevel()).status(entity.getStatus())
                .lastFollowUp(entity.getLastFollowUp()).remark(entity.getRemark())
                .company(entity.getCompany()).address(entity.getAddress())
                .industry(entity.getIndustry()).website(entity.getWebsite())
                .contactPerson(entity.getContactPerson())
                .tags(tags).notes(notes)
                .degraded(degraded)
                .createTime(entity.getCreateTime()).updateTime(entity.getUpdateTime())
                .build();
        dto.fillCenterFields(centerData);
        return dto;
    }

    private CustomerCenterData fetchCenterData(String idNumber) {
        if (idNumber == null) return null;
        try {
            return customerCenterGateway.findByIdNumber(idNumber).orElse(null);
        } catch (Exception e) {
            log.warn("获取center主数据失败，降级使用本地数据: {}", e.getMessage());
            return null;
        }
    }

    private CustomerNoteDTO toNoteDTO(com.harness.crm.domain.customer.entity.CustomerNoteEntity entity) {
        return CustomerNoteDTO.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .category(entity.getCategory())
                .content(entity.getContent())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    private void saveTags(Long customerId, List<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            tagService.validateTagIds(tagIds);
            tagService.createCustomerTagRelations(customerId, tagIds);
        }
    }

    private void replaceTags(Long customerId, List<Long> tagIds) {
        tagService.deleteCustomerTagRelations(customerId);
        saveTags(customerId, tagIds);
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
                .idType(dto.getIdType())
                .idNumber(dto.getIdNumber())
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
        if (dto.getIdType() != null) {
            entity.setIdType(dto.getIdType());
        }
    }

    private CustomerDTO toDTO(CustomerEntity entity) {
        List<TagDTO> tags = tagService.findTagsByCustomerId(entity.getId());
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
                .idType(entity.getIdType())
                .idNumber(entity.getIdNumber())
                .ccSyncStatus(entity.getCcSyncStatus())
                .tags(tags)
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
