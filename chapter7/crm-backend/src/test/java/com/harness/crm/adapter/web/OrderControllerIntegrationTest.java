package com.harness.crm.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harness.crm.app.customer.dto.CustomerDTO;
import com.harness.crm.app.opportunity.dto.OpportunityDTO;
import com.harness.crm.app.order.dto.OrderDTO;
import com.harness.crm.domain.customer.enums.CustomerLevel;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
import com.harness.crm.domain.order.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long createCustomer() throws Exception {
        CustomerDTO dto = CustomerDTO.builder()
                .name("测试客户")
                .source(CustomerSource.WEBSITE)
                .level(CustomerLevel.VIP)
                .status(CustomerStatus.ACTIVE)
                .build();

        MvcResult result = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn();

        return extractId(result);
    }

    private Long createOpportunity(Long customerId) throws Exception {
        OpportunityDTO dto = OpportunityDTO.builder()
                .title("测试机会")
                .customerId(customerId)
                .amount(new BigDecimal("100000"))
                .build();

        MvcResult result = mockMvc.perform(post("/api/opportunities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn();

        return extractId(result);
    }

    private OrderDTO buildOrderDTO(Long customerId, Long opportunityId) {
        return OrderDTO.builder()
                .customerId(customerId)
                .opportunityId(opportunityId)
                .totalAmount(new BigDecimal("50000"))
                .ownerName("张三")
                .remark("测试订单备注")
                .build();
    }

    private Long createOrder(Long customerId, Long opportunityId) throws Exception {
        OrderDTO dto = buildOrderDTO(customerId, opportunityId);
        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn();

        return extractId(result);
    }

    private Long extractId(MvcResult result) throws Exception {
        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseJson).path("data").path("id").asLong();
    }

    @Test
    void createOrder_shouldReturnCreatedOrder() throws Exception {
        Long customerId = createCustomer();
        Long opportunityId = createOpportunity(customerId);
        OrderDTO dto = buildOrderDTO(customerId, opportunityId);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.orderNo").isNotEmpty())
                .andExpect(jsonPath("$.data.customerId").value(customerId))
                .andExpect(jsonPath("$.data.opportunityId").value(opportunityId))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.ownerName").value("张三"))
                .andExpect(jsonPath("$.data.createTime").isNotEmpty())
                .andExpect(jsonPath("$.data.updateTime").isNotEmpty());
    }

    @Test
    void findById_shouldReturnOrder() throws Exception {
        Long customerId = createCustomer();
        Long opportunityId = createOpportunity(customerId);
        Long orderId = createOrder(customerId, opportunityId);

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(orderId))
                .andExpect(jsonPath("$.data.orderNo").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void updateOrder_shouldReturnUpdatedOrder() throws Exception {
        Long customerId = createCustomer();
        Long opportunityId = createOpportunity(customerId);
        Long orderId = createOrder(customerId, opportunityId);

        OrderDTO updateDto = OrderDTO.builder()
                .customerId(customerId)
                .opportunityId(opportunityId)
                .totalAmount(new BigDecimal("80000"))
                .status(OrderStatus.CONFIRMED)
                .ownerName("李四")
                .build();

        mockMvc.perform(put("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(orderId))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.ownerName").value("李四"));
    }

    @Test
    void updateOrder_terminalState_shouldFail() throws Exception {
        Long customerId = createCustomer();
        Long opportunityId = createOpportunity(customerId);
        Long orderId = createOrder(customerId, opportunityId);

        OrderDTO completeDto = OrderDTO.builder()
                .customerId(customerId)
                .opportunityId(opportunityId)
                .totalAmount(new BigDecimal("50000"))
                .status(OrderStatus.COMPLETED)
                .build();

        mockMvc.perform(put("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        OrderDTO updateDto = OrderDTO.builder()
                .customerId(customerId)
                .opportunityId(opportunityId)
                .totalAmount(new BigDecimal("99999"))
                .status(OrderStatus.PROCESSING)
                .build();

        mockMvc.perform(put("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }
}
