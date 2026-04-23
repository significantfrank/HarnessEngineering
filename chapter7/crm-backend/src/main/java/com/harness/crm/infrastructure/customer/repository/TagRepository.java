package com.harness.crm.infrastructure.customer.repository;

import com.harness.crm.domain.customer.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<TagEntity, Long> {

    Optional<TagEntity> findByName(String name);
}
