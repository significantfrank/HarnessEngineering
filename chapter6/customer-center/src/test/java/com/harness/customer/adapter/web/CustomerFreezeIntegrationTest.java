package com.harness.customer.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harness.customer.domain.entity.FreezeReason;
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
class CustomerFreezeIntegrationTest {

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

    private Long createCustomer() throws Exception {
        CustomerDTO dto = new CustomerDTO();
        dto.setName("张三");
        dto.setIdType(IdType.ID_CARD);
        dto.setIdNumber("110101199001011234");
        dto.setPhone("13800138000");
        dto.setEmail("zhangsan@example.com");
        dto.setGender(Gender.MALE);
        dto.setBirthday(LocalDate.of(1990, 1, 1));

        String response = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("data").get("id").asLong();
    }

    private Long freezeCustomer(Long id, FreezeReason reason) throws Exception {
        FreezeRequestDTO request = new FreezeRequestDTO(reason);
        String response = mockMvc.perform(put("/api/customers/{id}/freeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("data").get("id").asLong();
    }

    @Test
    void freezeCustomer_riskControl_success() throws Exception {
        Long id = createCustomer();
        FreezeRequestDTO request = new FreezeRequestDTO(FreezeReason.RISK_CONTROL);

        mockMvc.perform(put("/api/customers/{id}/freeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.data.accountStatus", is("FROZEN")))
                .andExpect(jsonPath("$.data.freezeReason", is("RISK_CONTROL")));
    }

    @Test
    void freezeCustomer_amlSuspicion_success() throws Exception {
        Long id = createCustomer();
        FreezeRequestDTO request = new FreezeRequestDTO(FreezeReason.AML_SUSPICION);

        mockMvc.perform(put("/api/customers/{id}/freeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountStatus", is("FROZEN")))
                .andExpect(jsonPath("$.data.freezeReason", is("AML_SUSPICION")));
    }

    @Test
    void freezeCustomer_judicial_success() throws Exception {
        Long id = createCustomer();
        FreezeRequestDTO request = new FreezeRequestDTO(FreezeReason.JUDICIAL);

        mockMvc.perform(put("/api/customers/{id}/freeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountStatus", is("FROZEN")))
                .andExpect(jsonPath("$.data.freezeReason", is("JUDICIAL")));
    }

    @Test
    void freezeCustomer_customerRequest_success() throws Exception {
        Long id = createCustomer();
        FreezeRequestDTO request = new FreezeRequestDTO(FreezeReason.CUSTOMER_REQUEST);

        mockMvc.perform(put("/api/customers/{id}/freeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountStatus", is("FROZEN")))
                .andExpect(jsonPath("$.data.freezeReason", is("CUSTOMER_REQUEST")));
    }

    @Test
    void freezeCustomer_alreadyFrozen() throws Exception {
        Long id = createCustomer();
        freezeCustomer(id, FreezeReason.RISK_CONTROL);

        FreezeRequestDTO request = new FreezeRequestDTO(FreezeReason.AML_SUSPICION);
        mockMvc.perform(put("/api/customers/{id}/freeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("400")))
                .andExpect(jsonPath("$.message", containsString("already frozen")));
    }

    @Test
    void freezeCustomer_cancelledAccount() throws Exception {
        Long id = createCustomer();
        customerJpaRepository.findById(id).ifPresent(c -> {
            c.cancelAccount();
            customerJpaRepository.save(c);
        });

        FreezeRequestDTO request = new FreezeRequestDTO(FreezeReason.RISK_CONTROL);
        mockMvc.perform(put("/api/customers/{id}/freeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("400")))
                .andExpect(jsonPath("$.message", containsString("Cancelled account cannot be frozen")));
    }

    @Test
    void freezeCustomer_notFound() throws Exception {
        FreezeRequestDTO request = new FreezeRequestDTO(FreezeReason.RISK_CONTROL);
        mockMvc.perform(put("/api/customers/{id}/freeze", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("400")))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void freezeCustomer_missingReason() throws Exception {
        Long id = createCustomer();
        mockMvc.perform(put("/api/customers/{id}/freeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("400")))
                .andExpect(jsonPath("$.message", containsString("reason")));
    }

    @Test
    void unfreezeCustomer_nonJudicial_success() throws Exception {
        Long id = createCustomer();
        freezeCustomer(id, FreezeReason.RISK_CONTROL);

        UnfreezeRequestDTO request = new UnfreezeRequestDTO("风险审查通过", null);
        mockMvc.perform(put("/api/customers/{id}/unfreeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.data.accountStatus", is("NORMAL")))
                .andExpect(jsonPath("$.data.unfreezeReason", is("风险审查通过")))
                .andExpect(jsonPath("$.data.freezeReason", is(nullValue())));
    }

    @Test
    void unfreezeCustomer_judicialWithValidAuth_success() throws Exception {
        Long id = createCustomer();
        freezeCustomer(id, FreezeReason.JUDICIAL);

        UnfreezeRequestDTO request = new UnfreezeRequestDTO("司法机关解冻", "JUD-2026-ABC12345");
        mockMvc.perform(put("/api/customers/{id}/unfreeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.data.accountStatus", is("NORMAL")))
                .andExpect(jsonPath("$.data.unfreezeReason", is("司法机关解冻")));
    }

    @Test
    void unfreezeCustomer_judicialWithoutAuth() throws Exception {
        Long id = createCustomer();
        freezeCustomer(id, FreezeReason.JUDICIAL);

        UnfreezeRequestDTO request = new UnfreezeRequestDTO("尝试解冻", null);
        mockMvc.perform(put("/api/customers/{id}/unfreeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("400")))
                .andExpect(jsonPath("$.message", containsString("authorization document")));
    }

    @Test
    void unfreezeCustomer_judicialWithInvalidAuth() throws Exception {
        Long id = createCustomer();
        freezeCustomer(id, FreezeReason.JUDICIAL);

        UnfreezeRequestDTO request = new UnfreezeRequestDTO("尝试解冻", "INVALID-DOC");
        mockMvc.perform(put("/api/customers/{id}/unfreeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("400")))
                .andExpect(jsonPath("$.message", containsString("Invalid judicial unfreeze authorization")));
    }

    @Test
    void unfreezeCustomer_notFrozen() throws Exception {
        Long id = createCustomer();

        UnfreezeRequestDTO request = new UnfreezeRequestDTO("解冻", null);
        mockMvc.perform(put("/api/customers/{id}/unfreeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("400")))
                .andExpect(jsonPath("$.message", containsString("Only frozen account can be unfrozen")));
    }

    @Test
    void unfreezeCustomer_notFound() throws Exception {
        UnfreezeRequestDTO request = new UnfreezeRequestDTO("解冻", null);
        mockMvc.perform(put("/api/customers/{id}/unfreeze", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("400")))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    void unfreezeCustomer_missingReason() throws Exception {
        Long id = createCustomer();
        freezeCustomer(id, FreezeReason.RISK_CONTROL);

        mockMvc.perform(put("/api/customers/{id}/unfreeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("400")))
                .andExpect(jsonPath("$.message", containsString("reason")));
    }

    @Test
    void freezeThenUnfreezeThenRefreeze_success() throws Exception {
        Long id = createCustomer();

        freezeCustomer(id, FreezeReason.RISK_CONTROL);

        UnfreezeRequestDTO unfreezeRequest = new UnfreezeRequestDTO("审查通过", null);
        mockMvc.perform(put("/api/customers/{id}/unfreeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unfreezeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountStatus", is("NORMAL")));

        FreezeRequestDTO refreezeRequest = new FreezeRequestDTO(FreezeReason.AML_SUSPICION);
        mockMvc.perform(put("/api/customers/{id}/freeze", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreezeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountStatus", is("FROZEN")))
                .andExpect(jsonPath("$.data.freezeReason", is("AML_SUSPICION")));
    }
}
