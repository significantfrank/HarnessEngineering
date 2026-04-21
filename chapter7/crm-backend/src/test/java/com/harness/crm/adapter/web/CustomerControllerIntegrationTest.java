package com.harness.crm.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harness.crm.app.customer.dto.CustomerDTO;
import com.harness.crm.domain.customer.enums.CustomerLevel;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerDTO buildCustomerDTO() {
        return CustomerDTO.builder()
                .name("测试客户")
                .phone("13800138000")
                .email("test@example.com")
                .company("测试公司")
                .address("测试地址")
                .source(CustomerSource.WEBSITE)
                .level(CustomerLevel.VIP)
                .industry("互联网")
                .website("https://example.com")
                .contactPerson("张三")
                .status(CustomerStatus.ACTIVE)
                .remark("测试备注")
                .build();
    }

    @Test
    void createCustomer_shouldReturnCreatedCustomer() throws Exception {
        CustomerDTO dto = buildCustomerDTO();

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("测试客户"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.company").value("测试公司"))
                .andExpect(jsonPath("$.data.source").value("WEBSITE"))
                .andExpect(jsonPath("$.data.level").value("VIP"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.createTime").isNotEmpty())
                .andExpect(jsonPath("$.data.updateTime").isNotEmpty());
    }

    @Test
    void updateCustomer_shouldReturnUpdatedCustomer() throws Exception {
        CustomerDTO createDto = buildCustomerDTO();

        MvcResult createResult = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        Long id = objectMapper.readTree(responseJson).path("data").path("id").asLong();

        CustomerDTO updateDto = CustomerDTO.builder()
                .name("更新客户")
                .phone("13900139000")
                .level(CustomerLevel.IMPORTANT)
                .status(CustomerStatus.INACTIVE)
                .build();

        mockMvc.perform(put("/api/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.name").value("更新客户"))
                .andExpect(jsonPath("$.data.phone").value("13900139000"))
                .andExpect(jsonPath("$.data.level").value("IMPORTANT"))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    void deleteCustomer_shouldReturnSuccess() throws Exception {
        CustomerDTO createDto = buildCustomerDTO();

        MvcResult createResult = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        Long id = objectMapper.readTree(responseJson).path("data").path("id").asLong();

        mockMvc.perform(delete("/api/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("success"));

        mockMvc.perform(get("/api/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("404"));
    }

    @Test
    void findById_shouldReturnCustomer() throws Exception {
        CustomerDTO createDto = buildCustomerDTO();

        MvcResult createResult = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        Long id = objectMapper.readTree(responseJson).path("data").path("id").asLong();

        mockMvc.perform(get("/api/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.name").value("测试客户"))
                .andExpect(jsonPath("$.data.source").value("WEBSITE"))
                .andExpect(jsonPath("$.data.level").value("VIP"));
    }

    @Test
    void listCustomers_shouldReturnPagedResult() throws Exception {
        CustomerDTO dto1 = CustomerDTO.builder().name("阿里云").source(CustomerSource.WEBSITE).level(CustomerLevel.VIP).build();
        CustomerDTO dto2 = CustomerDTO.builder().name("腾讯云").source(CustomerSource.REFERRAL).level(CustomerLevel.NORMAL).build();
        CustomerDTO dto3 = CustomerDTO.builder().name("阿里影业").source(CustomerSource.AD).level(CustomerLevel.VIP).build();

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto3)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/customers")
                        .param("name", "阿里")
                        .param("source", "WEBSITE")
                        .param("level", "VIP")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("阿里云"));
    }
}
