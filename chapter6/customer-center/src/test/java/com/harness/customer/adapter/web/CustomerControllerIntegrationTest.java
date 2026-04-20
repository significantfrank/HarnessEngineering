package com.harness.customer.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harness.customer.domain.entity.CustomerStatus;
import com.harness.customer.domain.entity.Gender;
import com.harness.customer.domain.entity.IdType;
import com.harness.customer.infrastructure.repository.CustomerJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerJpaRepository customerJpaRepository;

    @BeforeEach
    void cleanUp() {
        customerJpaRepository.deleteAll();
    }

    private CustomerDTO buildValidDTO() {
        CustomerDTO dto = new CustomerDTO();
        dto.setName("张三");
        dto.setIdType(IdType.ID_CARD);
        dto.setIdNumber("110101199001011234");
        dto.setPhone("13800138000");
        dto.setEmail("zhangsan@example.com");
        dto.setGender(Gender.MALE);
        dto.setBirthday(LocalDate.of(1990, 1, 1));
        return dto;
    }

    @Test
    void createCustomer_success() throws Exception {
        CustomerDTO dto = buildValidDTO();

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("张三")))
                .andExpect(jsonPath("$.idType", is("ID_CARD")))
                .andExpect(jsonPath("$.idNumber", is("110101199001011234")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.createTime", notNullValue()));
    }

    @Test
    void createCustomer_duplicateIdNumber() throws Exception {
        CustomerDTO dto = buildValidDTO();
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("already exists")));
    }

    @Test
    void createCustomer_validationError_blankName() throws Exception {
        CustomerDTO dto = buildValidDTO();
        dto.setName("");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("name")));
    }

    @Test
    void createCustomer_validationError_blankIdNumber() throws Exception {
        CustomerDTO dto = buildValidDTO();
        dto.setIdNumber(null);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("idNumber")));
    }

    @Test
    void getCustomer_success() throws Exception {
        CustomerDTO dto = buildValidDTO();
        String response = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.name", is("张三")));
    }

    @Test
    void getCustomer_notFound() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", 99999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("not found")));
    }

    @Test
    void listCustomers_all() throws Exception {
        CustomerDTO dto1 = buildValidDTO();
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto1)));

        CustomerDTO dto2 = buildValidDTO();
        dto2.setName("李四");
        dto2.setIdNumber("110101199002022345");
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto2)));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void listCustomers_filterByName() throws Exception {
        CustomerDTO dto1 = buildValidDTO();
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto1)));

        CustomerDTO dto2 = buildValidDTO();
        dto2.setName("李四");
        dto2.setIdNumber("110101199002022345");
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto2)));

        mockMvc.perform(get("/api/customers").param("name", "张"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("张三")));
    }

    @Test
    void listCustomers_filterByStatus() throws Exception {
        CustomerDTO dto = buildValidDTO();
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        mockMvc.perform(get("/api/customers").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/customers").param("status", "FROZEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void updateCustomer_success() throws Exception {
        CustomerDTO dto = buildValidDTO();
        String response = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        CustomerDTO updateDTO = buildValidDTO();
        updateDTO.setName("王五");
        updateDTO.setPhone("13900139000");

        mockMvc.perform(put("/api/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("王五")))
                .andExpect(jsonPath("$.phone", is("13900139000")));
    }

    @Test
    void updateCustomer_duplicateIdNumber() throws Exception {
        CustomerDTO dto1 = buildValidDTO();
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto1)));

        CustomerDTO dto2 = buildValidDTO();
        dto2.setName("李四");
        dto2.setIdNumber("110101199002022345");
        String response2 = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andReturn().getResponse().getContentAsString();
        Long id2 = objectMapper.readTree(response2).get("id").asLong();

        CustomerDTO updateDTO = buildValidDTO();
        updateDTO.setIdNumber("110101199001011234");

        mockMvc.perform(put("/api/customers/{id}", id2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("already exists")));
    }

    @Test
    void updateCustomer_notFound() throws Exception {
        CustomerDTO dto = buildValidDTO();
        mockMvc.perform(put("/api/customers/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("not found")));
    }

    @Test
    void deleteCustomer_success() throws Exception {
        CustomerDTO dto = buildValidDTO();
        String response = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/customers/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/customers/{id}", id))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCustomer_notFound() throws Exception {
        mockMvc.perform(delete("/api/customers/{id}", 99999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("not found")));
    }
}
