package com.harness.crm.adapter.web;

import com.harness.crm.adapter.web.common.ApiResponse;
import com.harness.crm.app.customer.CustomerService;
import com.harness.crm.app.customer.dto.CustomerDTO;
import com.harness.crm.domain.customer.enums.CustomerLevel;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
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

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ApiResponse<CustomerDTO> create(@Valid @RequestBody CustomerDTO dto) {
        return ApiResponse.success(customerService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerDTO> update(@PathVariable Long id, @Valid @RequestBody CustomerDTO dto) {
        return ApiResponse.success(customerService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        customerService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerDTO> findById(@PathVariable Long id) {
        return ApiResponse.success(customerService.findById(id));
    }

    @GetMapping
    public ApiResponse<Page<CustomerDTO>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) CustomerSource source,
            @RequestParam(required = false) CustomerLevel level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(customerService.findByConditions(name, status, source, level, page, size));
    }
}
