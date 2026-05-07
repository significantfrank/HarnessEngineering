package com.harness.crm.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harness.crm.app.customer.dto.CustomerDTO;
import com.harness.crm.app.customer.dto.CustomerNoteDTO;
import com.harness.crm.domain.customer.enums.CustomerLevel;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
import com.harness.crm.domain.customer.enums.NoteCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerNoteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long createCustomer() throws Exception {
        CustomerDTO dto = CustomerDTO.builder()
                .name("小记测试客户")
                .phone("13800138000")
                .source(CustomerSource.WEBSITE)
                .level(CustomerLevel.VIP)
                .status(CustomerStatus.ACTIVE)
                .build();

        MvcResult result = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    @Test
    void createNote_happyCase_shouldReturnCreatedNote() throws Exception {
        Long customerId = createCustomer();

        CustomerNoteDTO noteDTO = CustomerNoteDTO.builder()
                .category(NoteCategory.PHONE_CALL)
                .content("电话沟通了客户需求")
                .build();

        mockMvc.perform(post("/api/customers/{id}/notes", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.customerId").value(customerId))
                .andExpect(jsonPath("$.data.category").value("PHONE_CALL"))
                .andExpect(jsonPath("$.data.content").value("电话沟通了客户需求"))
                .andExpect(jsonPath("$.data.createTime").isNotEmpty());

        // 验证客户lastFollowUp被联动更新
        mockMvc.perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lastFollowUp").isNotEmpty());
    }

    @Test
    void createNote_customerNotFound_shouldReturn404() throws Exception {
        CustomerNoteDTO noteDTO = CustomerNoteDTO.builder()
                .category(NoteCategory.VISIT)
                .content("拜访客户")
                .build();

        mockMvc.perform(post("/api/customers/{id}/notes", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("404"));
    }
}
