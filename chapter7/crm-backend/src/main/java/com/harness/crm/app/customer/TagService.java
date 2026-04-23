package com.harness.crm.app.customer;

import com.harness.crm.app.customer.dto.TagDTO;
import com.harness.crm.domain.customer.entity.CustomerTagRelEntity;
import com.harness.crm.domain.customer.entity.TagEntity;
import com.harness.crm.domain.customer.gateway.CustomerTagGatewayI;
import com.harness.crm.domain.customer.gateway.TagGatewayI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagGatewayI tagGateway;
    private final CustomerTagGatewayI customerTagGateway;

    /** 创建标签 */
    @Transactional
    public TagDTO create(TagDTO dto) {
        tagGateway.findByName(dto.getName()).ifPresent(t -> {
            throw new RuntimeException("标签名称已存在: " + dto.getName());
        });
        TagEntity entity = toEntity(dto);
        entity.prePersist();
        TagEntity saved = tagGateway.save(entity);
        return toDTO(saved, 0L);
    }

    /** 更新标签 */
    public TagDTO update(Long id, TagDTO dto) {
        TagEntity existing = tagGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found: " + id));
        existing.setName(dto.getName());
        existing.setColor(dto.getColor());
        existing.preUpdate();
        TagEntity saved = tagGateway.save(existing);
        long count = customerTagGateway.countByTagId(saved.getId());
        return toDTO(saved, count);
    }

    /** 删除标签，使用中不可删除 */
    @Transactional
    public void deleteById(Long id) {
        long count = customerTagGateway.countByTagId(id);
        if (count > 0) {
            throw new RuntimeException("该标签正在被 " + count + " 个客户使用，无法删除");
        }
        tagGateway.deleteById(id);
    }

    /** 查询所有标签 */
    public List<TagDTO> findAll() {
        return tagGateway.findAll().stream()
                .map(tag -> toDTO(tag, customerTagGateway.countByTagId(tag.getId())))
                .collect(Collectors.toList());
    }

    /** 根据ID查询标签 */
    public TagDTO findById(Long id) {
        TagEntity entity = tagGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found: " + id));
        return toDTO(entity, customerTagGateway.countByTagId(entity.getId()));
    }

    /** 校验标签ID列表是否都存在 */
    public void validateTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return;
        for (Long tagId : tagIds) {
            tagGateway.findById(tagId)
                    .orElseThrow(() -> new RuntimeException("Tag not found: " + tagId));
        }
    }

    /** 为客户批量创建标签关联 */
    @Transactional
    public void createCustomerTagRelations(Long customerId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return;
        for (Long tagId : tagIds) {
            CustomerTagRelEntity rel = CustomerTagRelEntity.builder()
                    .customerId(customerId)
                    .tagId(tagId)
                    .build();
            rel.prePersist();
            customerTagGateway.save(rel);
        }
    }

    /** 删除客户所有标签关联 */
    @Transactional
    public void deleteCustomerTagRelations(Long customerId) {
        customerTagGateway.deleteByCustomerId(customerId);
    }

    /** 加载客户的标签列表 */
    public List<TagDTO> findTagsByCustomerId(Long customerId) {
        List<CustomerTagRelEntity> rels = customerTagGateway.findByCustomerId(customerId);
        return rels.stream()
                .map(rel -> tagGateway.findById(rel.getTagId()))
                .filter(opt -> opt.isPresent())
                .map(opt -> toDTO(opt.get(), null))
                .collect(Collectors.toList());
    }

    private TagEntity toEntity(TagDTO dto) {
        return TagEntity.builder()
                .name(dto.getName())
                .color(dto.getColor())
                .build();
    }

    private TagDTO toDTO(TagEntity entity, Long customerCount) {
        return TagDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .color(entity.getColor())
                .customerCount(customerCount)
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
