package com.harness.crm.domain.customer.gateway;

import com.harness.crm.domain.customer.entity.TagEntity;

import java.util.List;
import java.util.Optional;

public interface TagGatewayI {

    TagEntity save(TagEntity entity);

    Optional<TagEntity> findById(Long id);

    List<TagEntity> findAll();

    Optional<TagEntity> findByName(String name);

    void deleteById(Long id);
}
