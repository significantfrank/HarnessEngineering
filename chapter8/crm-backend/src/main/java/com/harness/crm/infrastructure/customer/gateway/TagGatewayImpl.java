package com.harness.crm.infrastructure.customer.gateway;

import com.harness.crm.domain.customer.entity.TagEntity;
import com.harness.crm.domain.customer.gateway.TagGatewayI;
import com.harness.crm.infrastructure.customer.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TagGatewayImpl implements TagGatewayI {

    private final TagRepository tagRepository;

    @Override
    public TagEntity save(TagEntity entity) {
        return tagRepository.save(entity);
    }

    @Override
    public Optional<TagEntity> findById(Long id) {
        return tagRepository.findById(id);
    }

    @Override
    public List<TagEntity> findAll() {
        return tagRepository.findAll();
    }

    @Override
    public Optional<TagEntity> findByName(String name) {
        return tagRepository.findByName(name);
    }

    @Override
    public void deleteById(Long id) {
        tagRepository.deleteById(id);
    }
}
