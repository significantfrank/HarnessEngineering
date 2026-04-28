package com.harness.crm.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harness.crm.app.customer.dto.CustomerDTO;
import com.harness.crm.app.order.dto.LegacyOrderDTO;
import com.harness.crm.domain.customer.enums.CustomerLevel;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
import com.harness.crm.domain.customer.gateway.CustomerCenterGatewayI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(statements = "CREATE TABLE IF NOT EXISTS audit_log (id BIGINT PRIMARY KEY AUTO_INCREMENT, biz_id VARCHAR(100) NOT NULL, action VARCHAR(100) NOT NULL, operator VARCHAR(100) NOT NULL DEFAULT 'SYSTEM', create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)")
class OrderNewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerCenterGatewayI customerCenterGateway;

    /** Happy Case：VIP客户下单LOAN产品，折扣后金额正确 */
    @Test
    void process_shouldReturnSuccessWithDiscountedAmount() throws Exception {
        Long customerId = createCustomer("VIP新引擎测试", CustomerLevel.VIP, CustomerStatus.ACTIVE);

        LegacyOrderDTO request = LegacyOrderDTO.builder()
                .customerId(customerId)
                .opportunityId(1L)
                .totalAmount(new BigDecimal("2000000"))
                .email("vip@example.comzzz")
                .customerLevel("VIP")
                .productType("LOAN")
                .ownerName("张三")
                .remark("新引擎订单测试")
                .build();

        mockMvc.perform(post("/api/orders/process")
                        .param("useNew", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.customerId").value(customerId.intValue()))
                .andExpect(jsonPath("$.data.totalAmount").value(1800000.0))
                .andExpect(jsonPath("$.data.customerLevel").value("VIP"))
                .andExpect(jsonPath("$.data.productType").value("LOAN"))
                .andExpect(jsonPath("$.data.ownerName").value("张三"));
    }

    /** Bad Case：NORMAL客户购买FUND产品，合规校验拒绝 */
    @Test
    void process_shouldReturnErrorWhenComplianceCheckFails() throws Exception {
        Long customerId = createCustomer("NORMAL合规测试", CustomerLevel.NORMAL, CustomerStatus.ACTIVE);

        LegacyOrderDTO request = LegacyOrderDTO.builder()
                .customerId(customerId)
                .opportunityId(1L)
                .totalAmount(new BigDecimal("100000"))
                .email("normal@example.com")
                .customerLevel("NORMAL")
                .productType("FUND")
                .ownerName("李四")
                .remark("合规校验失败测试")
                .build();

        mockMvc.perform(post("/api/orders/process")
                        .param("useNew", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.message").value("客户等级不足以购买理财产品，拒绝下单"));
    }

    private Long createCustomer(String name, CustomerLevel level, CustomerStatus status) throws Exception {
        CustomerDTO dto = CustomerDTO.builder()
                .name(name)
                .level(level)
                .status(status)
                .email("normal@example.com")
                .phone("13681874561")
                .source(CustomerSource.WEBSITE)
                .idType("ID_CARD")
                .idNumber("NEW_ORDER_TEST_" + System.nanoTime())
                .build();

        MvcResult result = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }
}
