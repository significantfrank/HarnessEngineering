package com.harness.crm.app.customer;

import com.harness.crm.app.customer.dto.CustomerNoteDTO;
import com.harness.crm.domain.customer.entity.CustomerEntity;
import com.harness.crm.domain.customer.entity.CustomerNoteEntity;
import com.harness.crm.domain.customer.gateway.CustomerGatewayI;
import com.harness.crm.domain.customer.gateway.CustomerNoteGatewayI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerNoteService {

    private final CustomerNoteGatewayI customerNoteGateway;
    private final CustomerGatewayI customerGateway;

    /**
     * 新增客户小记，联动更新客户最后跟进时间
     */
    @Transactional
    public CustomerNoteDTO create(Long customerId, CustomerNoteDTO dto) {
        CustomerEntity customer = customerGateway.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        CustomerNoteEntity entity = toEntity(customerId, dto);
        entity.prePersist();
        CustomerNoteEntity saved = customerNoteGateway.save(entity);

        // 领域对象封装的联动逻辑：更新客户最后跟进时间
        saved.updateCustomerLastFollowUp(customer);
        customer.preUpdate();
        customerGateway.save(customer);

        return toDTO(saved);
    }

    /**
     * 分页查询客户小记，按创建时间倒序
     */
    public Page<CustomerNoteDTO> findByCustomerId(Long customerId, int page, int size) {
        customerGateway.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        Page<CustomerNoteEntity> entityPage = customerNoteGateway.findByCustomerId(customerId, PageRequest.of(page, size));
        return entityPage.map(this::toDTO);
    }

    private CustomerNoteEntity toEntity(Long customerId, CustomerNoteDTO dto) {
        return CustomerNoteEntity.builder()
                .customerId(customerId)
                .category(dto.getCategory())
                .content(dto.getContent())
                .build();
    }

    private CustomerNoteDTO toDTO(CustomerNoteEntity entity) {
        return CustomerNoteDTO.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .category(entity.getCategory())
                .content(entity.getContent())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
