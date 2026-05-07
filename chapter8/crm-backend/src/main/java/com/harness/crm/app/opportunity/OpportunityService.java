package com.harness.crm.app.opportunity;

import com.harness.crm.app.opportunity.dto.OppWinDTO;
import com.harness.crm.app.opportunity.dto.OpportunityDTO;
import com.harness.crm.domain.opportunity.entity.OpportunityEntity;
import com.harness.crm.domain.opportunity.enums.OppStage;
import com.harness.crm.domain.opportunity.gateway.OpportunityGatewayI;
import com.harness.crm.domain.order.entity.OrderEntity;
import com.harness.crm.domain.order.enums.OrderStatus;
import com.harness.crm.domain.order.gateway.OrderGatewayI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpportunityService {

    private final OpportunityGatewayI opportunityGateway;
    private final OrderGatewayI orderGateway;

    public OpportunityDTO create(OpportunityDTO dto) {
        OpportunityEntity entity = toEntity(dto);
        entity.prePersist();
        OpportunityEntity saved = opportunityGateway.save(entity);
        return toDTO(saved);
    }

    /**
     * 更新机会，终态不可更新
     */
    public OpportunityDTO update(Long id, OpportunityDTO dto) {
        OpportunityEntity existing = opportunityGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found: " + id));
        if (existing.isTerminal()) {
            throw new RuntimeException("机会已处于终态，不可更新: " + existing.getStage());
        }
        updateEntity(existing, dto);
        existing.preUpdate();
        OpportunityEntity saved = opportunityGateway.save(existing);
        return toDTO(saved);
    }

    public OpportunityDTO findById(Long id) {
        OpportunityEntity entity = opportunityGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found: " + id));
        return toDTO(entity);
    }

    public Page<OpportunityDTO> findByConditions(String title, OppStage stage, Long customerId, String ownerName, int page, int size) {
        Page<OpportunityEntity> entityPage = opportunityGateway.findByConditions(title, stage, customerId, ownerName, PageRequest.of(page, size));
        return entityPage.map(this::toDTO);
    }

    public void deleteById(Long id) {
        opportunityGateway.deleteById(id);
    }

    /**
     * 获取看板数据，按阶段分组
     */
    public Map<OppStage, ?> getKanbanData() {
        List<OpportunityEntity> all = opportunityGateway.findAllForKanban();
        Map<OppStage, List<OpportunityEntity>> grouped = all.stream()
                .collect(Collectors.groupingBy(OpportunityEntity::getStage, () -> new EnumMap<>(OppStage.class), Collectors.toList()));
        for (OppStage stage : OppStage.values()) {
            grouped.putIfAbsent(stage, List.of());
        }
        Map<OppStage, List<OpportunityDTO>> result = new EnumMap<>(OppStage.class);
        for (Map.Entry<OppStage, List<OpportunityEntity>> entry : grouped.entrySet()) {
            result.put(entry.getKey(), entry.getValue().stream().map(this::toDTO).toList());
        }
        return result;
    }

    /**
     * 更新阶段（拖拽），终态不可变更
     */
    public OpportunityDTO updateStage(Long id, OppStage targetStage) {
        OpportunityEntity entity = opportunityGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found: " + id));
        if (entity.isTerminal()) {
            throw new RuntimeException("机会已处于终态 " + entity.getStage() + "，不可变更阶段");
        }
        entity.setStage(targetStage);
        entity.preUpdate();
        OpportunityEntity updated = opportunityGateway.save(entity);
        return toDTO(updated);
    }

    /**
     * 赢单：标记阶段为WON，创建首个订单
     */
    @Transactional
    public Map<String, Object> win(Long id, OppWinDTO winDTO) {
        OpportunityEntity opp = opportunityGateway.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found: " + id));

        if (opp.isTerminal()) {
            throw new RuntimeException("机会已处于终态，不可赢单: " + opp.getStage());
        }

        opp.setStage(OppStage.WON);
        opp.preUpdate();
        opportunityGateway.save(opp);

        OrderEntity order = OrderEntity.builder()
                .customerId(opp.getCustomerId())
                .opportunityId(opp.getId())
                .totalAmount(winDTO.getAmount())
                .status(OrderStatus.PENDING)
                .ownerName(opp.getOwnerName())
                .remark(winDTO.getRemark())
                .build();
        order.generateOrderNo(orderGateway);
        order.prePersist();
        OrderEntity savedOrder = orderGateway.save(order);

        return Map.of(
                "opportunity", toDTO(opp),
                "order", Map.of(
                        "id", savedOrder.getId(),
                        "orderNo", savedOrder.getOrderNo(),
                        "totalAmount", savedOrder.getTotalAmount(),
                        "status", savedOrder.getStatus().name()
                )
        );
    }

    private OpportunityEntity toEntity(OpportunityDTO dto) {
        return OpportunityEntity.builder()
                .title(dto.getTitle())
                .customerId(dto.getCustomerId())
                .amount(dto.getAmount())
                .stage(dto.getStage())
                .probability(dto.getProbability())
                .expectedCloseDate(dto.getExpectedCloseDate())
                .leadId(dto.getLeadId())
                .ownerName(dto.getOwnerName())
                .remark(dto.getRemark())
                .build();
    }

    private void updateEntity(OpportunityEntity entity, OpportunityDTO dto) {
        entity.setTitle(dto.getTitle());
        entity.setCustomerId(dto.getCustomerId());
        entity.setAmount(dto.getAmount());
        entity.setStage(dto.getStage());
        entity.setProbability(dto.getProbability());
        entity.setExpectedCloseDate(dto.getExpectedCloseDate());
        entity.setLeadId(dto.getLeadId());
        entity.setOwnerName(dto.getOwnerName());
        entity.setRemark(dto.getRemark());
    }

    private OpportunityDTO toDTO(OpportunityEntity entity) {
        return OpportunityDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .customerId(entity.getCustomerId())
                .amount(entity.getAmount())
                .stage(entity.getStage())
                .probability(entity.getProbability())
                .expectedCloseDate(entity.getExpectedCloseDate())
                .leadId(entity.getLeadId())
                .ownerName(entity.getOwnerName())
                .remark(entity.getRemark())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
