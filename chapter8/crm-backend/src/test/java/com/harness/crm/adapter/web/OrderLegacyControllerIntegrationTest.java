package com.harness.crm.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harness.crm.app.order.LegacyOrderService;
import com.harness.crm.app.order.dto.LegacyOrderDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderLegacyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LegacyOrderService legacyOrderService;

    private LegacyOrderDTO buildLegacyOrderDTO() {
        return LegacyOrderDTO.builder()
                .customerId(1L)
                .opportunityId(1L)
                .totalAmount(new BigDecimal("2000000"))
                .email("test@example.com")
                .customerLevel("VIP")
                .productType("LOAN")
                .ownerName("张三")
                .remark("遗留订单测试")
                .build();
    }

    @Test
    void process_shouldReturnSuccessWhenLegacyServiceSucceeds() throws Exception {
        LegacyOrderDTO request = buildLegacyOrderDTO();
        LegacyOrderDTO response = buildLegacyOrderDTO();
        response.setTotalAmount(new BigDecimal("1900000"));
        when(legacyOrderService.processOrder(any(LegacyOrderDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.customerId").value(1))
                .andExpect(jsonPath("$.data.totalAmount").value(1900000.0))
                .andExpect(jsonPath("$.data.customerLevel").value("VIP"))
                .andExpect(jsonPath("$.data.productType").value("LOAN"))
                .andExpect(jsonPath("$.data.ownerName").value("张三"));
    }

    @Test
    void process_shouldReturnErrorWhenLegacyServiceThrowsRuntimeException() throws Exception {
        LegacyOrderDTO request = buildLegacyOrderDTO();
        when(legacyOrderService.processOrder(any(LegacyOrderDTO.class)))
                .thenThrow(new RuntimeException("交易失败，请联系客户经理"));

        mockMvc.perform(post("/api/orders/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.message").value("交易失败，请联系客户经理"));
    }

    @Test
    void process_shouldReturnErrorWhenComplianceCheckFails() throws Exception {
        LegacyOrderDTO request = buildLegacyOrderDTO();
        when(legacyOrderService.processOrder(any(LegacyOrderDTO.class)))
                .thenThrow(new RuntimeException("客户未通过1合规校验，拒绝下单"));

        mockMvc.perform(post("/api/orders/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.message").value("客户未通过1合规校验，拒绝下单"));
    }

    @Test
    void process_shouldReturnErrorWhenRequestIsNullCustomerId() throws Exception {
        when(legacyOrderService.processOrder(any(LegacyOrderDTO.class)))
                .thenThrow(new IllegalArgumentException("请求参数缺失"));

        LegacyOrderDTO request = LegacyOrderDTO.builder()
                .customerId(null)
                .opportunityId(1L)
                .totalAmount(new BigDecimal("100000"))
                .email("test@example.com")
                .customerLevel("VIP")
                .productType("LOAN")
                .ownerName("张三")
                .build();

        mockMvc.perform(post("/api/orders/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    void process_shouldReturnErrorWhenRequestIsNullTotalAmount() throws Exception {
        when(legacyOrderService.processOrder(any(LegacyOrderDTO.class)))
                .thenThrow(new IllegalArgumentException("请求参数缺失"));

        LegacyOrderDTO request = LegacyOrderDTO.builder()
                .customerId(1L)
                .opportunityId(1L)
                .totalAmount(null)
                .email("test@example.com")
                .customerLevel("VIP")
                .productType("LOAN")
                .ownerName("张三")
                .build();

        mockMvc.perform(post("/api/orders/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    void process_shouldReturnDiscountedAmountWhenDiscountApplied() throws Exception {
        LegacyOrderDTO request = LegacyOrderDTO.builder()
                .customerId(1L)
                .opportunityId(1L)
                .totalAmount(new BigDecimal("1000000"))
                .email("test@example.com")
                .customerLevel("VIP")
                .productType("FUND")
                .ownerName("李四")
                .remark("VIP大额订单")
                .build();

        LegacyOrderDTO response = LegacyOrderDTO.builder()
                .customerId(1L)
                .opportunityId(1L)
                .totalAmount(new BigDecimal("950000"))
                .email("test@example.com")
                .customerLevel("VIP")
                .productType("FUND")
                .ownerName("李四")
                .remark("VIP大额订单")
                .build();
        when(legacyOrderService.processOrder(any(LegacyOrderDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.totalAmount").value(950000.0))
                .andExpect(jsonPath("$.data.productType").value("FUND"))
                .andExpect(jsonPath("$.data.ownerName").value("李四"));
    }
}
