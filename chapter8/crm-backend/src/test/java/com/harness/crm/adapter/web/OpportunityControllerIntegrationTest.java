package com.harness.crm.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harness.crm.app.customer.dto.CustomerDTO;
import com.harness.crm.app.opportunity.dto.OppWinDTO;
import com.harness.crm.app.opportunity.dto.OpportunityDTO;
import com.harness.crm.domain.customer.enums.CustomerLevel;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
import com.harness.crm.domain.opportunity.enums.OppStage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpportunityControllerIntegrationTest {

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

    private OpportunityDTO buildOpportunityDTO(Long customerId) {
        return OpportunityDTO.builder()
                .title("测试机会")
                .customerId(customerId)
                .amount(new BigDecimal("100000"))
                .expectedCloseDate(LocalDate.now().plusMonths(3))
                .ownerName("张三")
                .remark("测试备注")
                .build();
    }

    private Long createOpportunity(Long customerId) throws Exception {
        OpportunityDTO dto = buildOpportunityDTO(customerId);
        MvcResult result = mockMvc.perform(post("/api/opportunities")
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
    void createOpportunity_shouldReturnCreatedOpportunity() throws Exception {
        Long customerId = createCustomer();
        OpportunityDTO dto = buildOpportunityDTO(customerId);

        mockMvc.perform(post("/api/opportunities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("测试机会"))
                .andExpect(jsonPath("$.data.customerId").value(customerId))
                .andExpect(jsonPath("$.data.stage").value("PROSPECTING"))
                .andExpect(jsonPath("$.data.ownerName").value("张三"))
                .andExpect(jsonPath("$.data.createTime").isNotEmpty())
                .andExpect(jsonPath("$.data.updateTime").isNotEmpty());
    }

    @Test
    void findById_shouldReturnOpportunity() throws Exception {
        Long customerId = createCustomer();
        Long oppId = createOpportunity(customerId);

        mockMvc.perform(get("/api/opportunities/{id}", oppId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(oppId))
                .andExpect(jsonPath("$.data.title").value("测试机会"))
                .andExpect(jsonPath("$.data.stage").value("PROSPECTING"));
    }

    @Test
    void deleteOpportunity_shouldReturnSuccess() throws Exception {
        Long customerId = createCustomer();
        Long oppId = createOpportunity(customerId);

        mockMvc.perform(delete("/api/opportunities/{id}", oppId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("success"));

        mockMvc.perform(get("/api/opportunities/{id}", oppId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("404"));
    }

    @Test
    void updateStage_shouldChangeStage() throws Exception {
        Long customerId = createCustomer();
        Long oppId = createOpportunity(customerId);

        mockMvc.perform(patch("/api/opportunities/{id}/stage", oppId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("stage", "QUALIFYING"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(oppId))
                .andExpect(jsonPath("$.data.stage").value("QUALIFYING"));
    }

    @Test
    void updateStage_terminalState_shouldFail() throws Exception {
        Long customerId = createCustomer();
        Long oppId = createOpportunity(customerId);

        OppWinDTO winDTO = OppWinDTO.builder()
                .amount(new BigDecimal("100000"))
                .build();

        mockMvc.perform(post("/api/opportunities/{id}/win", oppId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(winDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        mockMvc.perform(patch("/api/opportunities/{id}/stage", oppId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("stage", "QUALIFYING"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    void winOpportunity_shouldCreateOrder() throws Exception {
        Long customerId = createCustomer();
        Long oppId = createOpportunity(customerId);

        OppWinDTO winDTO = OppWinDTO.builder()
                .amount(new BigDecimal("100000"))
                .remark("赢单备注")
                .build();

        mockMvc.perform(post("/api/opportunities/{id}/win", oppId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(winDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.opportunity.stage").value("WON"))
                .andExpect(jsonPath("$.data.order.id").isNumber())
                .andExpect(jsonPath("$.data.order.orderNo").isNotEmpty())
                .andExpect(jsonPath("$.data.order.status").value("PENDING"));
    }

    @Test
    void winOpportunity_terminalState_shouldFail() throws Exception {
        Long customerId = createCustomer();
        Long oppId = createOpportunity(customerId);

        OppWinDTO winDTO = OppWinDTO.builder()
                .amount(new BigDecimal("100000"))
                .build();

        mockMvc.perform(post("/api/opportunities/{id}/win", oppId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(winDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        mockMvc.perform(post("/api/opportunities/{id}/win", oppId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(winDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }
}
