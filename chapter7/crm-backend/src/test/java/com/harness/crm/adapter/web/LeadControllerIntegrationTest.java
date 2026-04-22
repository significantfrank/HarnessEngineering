package com.harness.crm.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harness.crm.app.lead.dto.LeadConvertDTO;
import com.harness.crm.app.lead.dto.LeadDTO;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.lead.enums.LeadStatus;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private LeadDTO buildLeadDTO() {
        return LeadDTO.builder()
                .name("测试线索")
                .phone("13800138000")
                .email("lead@example.com")
                .company("测试公司")
                .source(CustomerSource.WEBSITE)
                .ownerName("张三")
                .remark("测试备注")
                .build();
    }

    private Long extractId(MvcResult result) throws Exception {
        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseJson).path("data").path("id").asLong();
    }

    @Test
    void createLead_shouldReturnCreatedLead() throws Exception {
        LeadDTO dto = buildLeadDTO();

        mockMvc.perform(post("/api/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("测试线索"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.source").value("WEBSITE"))
                .andExpect(jsonPath("$.data.status").value("NEW"))
                .andExpect(jsonPath("$.data.createTime").isNotEmpty())
                .andExpect(jsonPath("$.data.updateTime").isNotEmpty());
    }

    @Test
    void updateLead_shouldReturnUpdatedLead() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildLeadDTO())))
                .andReturn();

        Long id = extractId(createResult);

        LeadDTO updateDto = LeadDTO.builder()
                .name("更新线索")
                .phone("13900139000")
                .status(LeadStatus.CONTACTED)
                .build();

        mockMvc.perform(put("/api/leads/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.name").value("更新线索"))
                .andExpect(jsonPath("$.data.phone").value("13900139000"))
                .andExpect(jsonPath("$.data.status").value("CONTACTED"));
    }

    @Test
    void deleteLead_shouldReturnSuccess() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildLeadDTO())))
                .andReturn();

        Long id = extractId(createResult);

        mockMvc.perform(delete("/api/leads/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("success"));

        mockMvc.perform(get("/api/leads/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("404"));
    }

    @Test
    void findById_shouldReturnLead() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildLeadDTO())))
                .andReturn();

        Long id = extractId(createResult);

        mockMvc.perform(get("/api/leads/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.name").value("测试线索"))
                .andExpect(jsonPath("$.data.source").value("WEBSITE"));
    }

    @Test
    void convertLead_shouldCreateCustomerAndOpportunity() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildLeadDTO())))
                .andReturn();

        Long id = extractId(createResult);

        LeadConvertDTO convertDTO = LeadConvertDTO.builder()
                .opportunityTitle("测试机会")
                .amount(new BigDecimal("50000"))
                .expectedCloseDate(LocalDate.now().plusMonths(3))
                .build();

        mockMvc.perform(post("/api/leads/{id}/convert", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(convertDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.customer.id").isNumber())
                .andExpect(jsonPath("$.data.customer.name").value("测试线索"))
                .andExpect(jsonPath("$.data.opportunity.id").isNumber())
                .andExpect(jsonPath("$.data.opportunity.title").value("测试机会"))
                .andExpect(jsonPath("$.data.opportunity.stage").value("PROSPECTING"));

        mockMvc.perform(get("/api/leads/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONVERTED"));
    }

    @Test
    void convertLead_alreadyConverted_shouldFail() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildLeadDTO())))
                .andReturn();

        Long id = extractId(createResult);

        LeadConvertDTO convertDTO = LeadConvertDTO.builder()
                .opportunityTitle("测试机会")
                .amount(new BigDecimal("50000"))
                .build();

        mockMvc.perform(post("/api/leads/{id}/convert", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(convertDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        mockMvc.perform(post("/api/leads/{id}/convert", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(convertDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }
}
