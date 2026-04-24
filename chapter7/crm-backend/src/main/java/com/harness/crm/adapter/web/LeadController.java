package com.harness.crm.adapter.web;

import com.harness.crm.adapter.web.common.ApiResponse;
import com.harness.crm.app.lead.LeadService;
import com.harness.crm.app.lead.dto.LeadConvertDTO;
import com.harness.crm.app.lead.dto.LeadDTO;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.lead.enums.LeadStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    public ApiResponse<LeadDTO> create(@Valid @RequestBody LeadDTO dto) {
        return ApiResponse.success(leadService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<LeadDTO> update(@PathVariable Long id, @Valid @RequestBody LeadDTO dto) {
        return ApiResponse.success(leadService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        leadService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse<LeadDTO> findById(@PathVariable Long id) {
        return ApiResponse.success(leadService.findById(id));
    }

    @GetMapping
    public ApiResponse<Page<LeadDTO>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) CustomerSource source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(leadService.findByConditions(name, status, source, page, size));
    }

    /**
     * 线索转化：创建Customer+Opportunity + center同步
     */
    @PostMapping("/{id}/convert")
    public ApiResponse<Map<String, Object>> convert(@PathVariable Long id, @Valid @RequestBody LeadConvertDTO convertDTO) {
        Map<String, Object> result = leadService.convert(id, convertDTO);
        Long customerId = (Long) result.get("customerId");
        Long opportunityId = (Long) result.get("opportunityId");
        String customerName = (String) result.get("customerName");
        String customerPhone = (String) result.get("customerPhone");
        String customerEmail = (String) result.get("customerEmail");
        String customerIdType = (String) result.get("customerIdType");
        String customerIdNumber = (String) result.get("customerIdNumber");
        String originalStatus = (String) result.get("originalStatus");
        Long originalCustomerId = (Long) result.get("originalCustomerId");

        leadService.syncAfterConvert(
                customerId, opportunityId, id,
                originalStatus, originalCustomerId,
                customerName, customerPhone, customerEmail, customerIdType, customerIdNumber
        );

        return ApiResponse.success(Map.of(
                "customerId", customerId,
                "opportunityId", opportunityId
        ));
    }
}
