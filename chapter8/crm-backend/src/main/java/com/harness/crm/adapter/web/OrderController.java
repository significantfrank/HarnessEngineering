package com.harness.crm.adapter.web;

import com.harness.crm.adapter.web.common.ApiResponse;
import com.harness.crm.app.order.convertor.OrderConvertor;
import com.harness.crm.app.order.OrderService;
import com.harness.crm.app.order.LegacyOrderService;
import com.harness.crm.app.order.dto.LegacyOrderDTO;
import com.harness.crm.app.order.dto.OrderDTO;
import com.harness.crm.domain.order.enums.OrderStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OrderController {

    private final OrderService orderService;

    private final LegacyOrderService legacyOrderService;

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

    @PostMapping("/process")
    public ApiResponse<LegacyOrderDTO> process(@RequestParam(name = "useNew", defaultValue = "false") Boolean useNew, @RequestBody LegacyOrderDTO dto) {
        log.info("收到订单处理请求，模式：{}", useNew ? " 新引擎" : "老逻辑");

        LegacyOrderDTO result;
        if (useNew) {
            // 绞杀者：调用重构后的新代码
            OrderDTO orderDTO = OrderConvertor.toOrderDTO(dto);
            OrderDTO processed = orderService.processOrder(orderDTO);
            result = OrderConvertor.toLegacyOrderDTO(processed);
        } else {
            // 调用原本的泥潭代码
            result = legacyOrderService.processOrder(dto);
        }

        return ApiResponse.success(result);
    }
}
