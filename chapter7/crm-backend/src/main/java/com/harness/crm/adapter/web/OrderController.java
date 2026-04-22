package com.harness.crm.adapter.web;

import com.harness.crm.adapter.web.common.ApiResponse;
import com.harness.crm.app.order.OrderService;
import com.harness.crm.app.order.dto.OrderDTO;
import com.harness.crm.domain.order.enums.OrderStatus;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderDTO> create(@Valid @RequestBody OrderDTO dto) {
        return ApiResponse.success(orderService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<OrderDTO> update(@PathVariable Long id, @Valid @RequestBody OrderDTO dto) {
        return ApiResponse.success(orderService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        orderService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDTO> findById(@PathVariable Long id) {
        return ApiResponse.success(orderService.findById(id));
    }

    @GetMapping
    public ApiResponse<Page<OrderDTO>> list(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long opportunityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(orderService.findByConditions(orderNo, status, customerId, opportunityId, page, size));
    }
}
