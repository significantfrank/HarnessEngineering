package com.harness.crm.adapter.web;

import com.harness.crm.adapter.web.common.ApiResponse;
import com.harness.crm.app.opportunity.OpportunityService;
import com.harness.crm.app.opportunity.dto.OppWinDTO;
import com.harness.crm.app.opportunity.dto.OpportunityDTO;
import com.harness.crm.domain.opportunity.enums.OppStage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityService opportunityService;

    @PostMapping
    public ApiResponse<OpportunityDTO> create(@Valid @RequestBody OpportunityDTO dto) {
        return ApiResponse.success(opportunityService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<OpportunityDTO> update(@PathVariable Long id, @Valid @RequestBody OpportunityDTO dto) {
        return ApiResponse.success(opportunityService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        opportunityService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse<OpportunityDTO> findById(@PathVariable Long id) {
        return ApiResponse.success(opportunityService.findById(id));
    }

    @GetMapping
    public ApiResponse<Page<OpportunityDTO>> list(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) OppStage stage,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String ownerName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(opportunityService.findByConditions(title, stage, customerId, ownerName, page, size));
    }

    /**
     * 看板数据：按阶段分组
     */
    @GetMapping("/kanban")
    public ApiResponse<Map<OppStage, ?>> kanban() {
        return ApiResponse.success(opportunityService.getKanbanData());
    }

    /**
     * 拖拽更新阶段
     */
    @PatchMapping("/{id}/stage")
    public ApiResponse<OpportunityDTO> updateStage(@PathVariable Long id, @RequestBody Map<String, OppStage> body) {
        return ApiResponse.success(opportunityService.updateStage(id, body.get("stage")));
    }

    /**
     * 赢单：标记WON并创建订单
     */
    @PostMapping("/{id}/win")
    public ApiResponse<Map<String, Object>> win(@PathVariable Long id, @Valid @RequestBody OppWinDTO winDTO) {
        return ApiResponse.success(opportunityService.win(id, winDTO));
    }
}
