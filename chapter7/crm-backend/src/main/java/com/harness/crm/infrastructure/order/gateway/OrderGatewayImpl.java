package com.harness.crm.infrastructure.order.gateway;

import com.harness.crm.domain.order.entity.OrderEntity;
import com.harness.crm.domain.order.enums.OrderStatus;
import com.harness.crm.domain.order.gateway.OrderGatewayI;
import com.harness.crm.infrastructure.order.repository.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderGatewayImpl implements OrderGatewayI {

    private final OrderRepository orderRepository;

    @Override
    public OrderEntity save(OrderEntity entity) {
        return orderRepository.save(entity);
    }

    @Override
    public Optional<OrderEntity> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public Page<OrderEntity> findByConditions(String orderNo, OrderStatus status, Long customerId, Long opportunityId, Pageable pageable) {
        Specification<OrderEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (orderNo != null && !orderNo.isBlank()) {
                predicates.add(cb.equal(root.get("orderNo"), orderNo));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customerId"), customerId));
            }
            if (opportunityId != null) {
                predicates.add(cb.equal(root.get("opportunityId"), opportunityId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return orderRepository.findAll(spec, pageable);
    }

    @Override
    public String findMaxOrderNoByDate(String datePrefix) {
        return orderRepository.findMaxOrderNoByPrefix(datePrefix);
    }
}
