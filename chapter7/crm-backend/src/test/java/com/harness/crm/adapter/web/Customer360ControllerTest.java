package com.harness.crm.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.harness.crm.app.customer.dto.CustomerDTO;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Customer360ControllerTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("customer-center.base-url", () -> "http://localhost:" + wireMock.getRuntimeInfo().getHttpPort());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll();
    }

    @Test
    void getCustomer360_shouldReturn360View() throws Exception {
        String successBody = objectMapper.writeValueAsString(
                objectMapper.readTree(getClass().getResourceAsStream("/fixture/customer_center_success.json")));

        wireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/customers/idNumber"))
                .withQueryParam("idNumber", equalTo("360ID001"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(successBody)));

        wireMock.stubFor(WireMock.put(WireMock.urlPathMatching("/api/customers/\\d+"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\":\"200\",\"message\":\"success\"}")));

        CustomerDTO dto = CustomerDTO.builder()
                .name("360测试客户")
                .phone("13800003600")
                .email("360@test.com")
                .idType("ID_CARD")
                .idNumber("360ID001")
                .source(CustomerSource.WEBSITE)
                .status(CustomerStatus.ACTIVE)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        Long id = objectMapper.readTree(responseJson).path("data").path("id").asLong();

        mockMvc.perform(get("/api/customers/{id}/360", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.name").value("张三"))
                .andExpect(jsonPath("$.data.degraded").value(false))
                .andExpect(jsonPath("$.data.memberLevel").value("GOLD"))
                .andExpect(jsonPath("$.data.authLevel").value("L2"))
                .andExpect(jsonPath("$.data.aum").value(500000.00))
                .andExpect(jsonPath("$.data.holdingProducts").isArray())
                .andExpect(jsonPath("$.data.holdingProducts.length()").value(2));
    }

    @Test
    void getCustomer360_shouldReturnDegradedViewWhenCenterFail() throws Exception {
        String failBody = objectMapper.writeValueAsString(
                objectMapper.readTree(getClass().getResourceAsStream("/fixture/customer_center_fail.json")));

        wireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/customers/idNumber"))
                .withQueryParam("idNumber", equalTo("FAILID001"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(failBody)));

        wireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/api/customers"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(201)
                        .withBody("{\"code\":\"200\",\"message\":\"success\",\"data\":{\"id\":1}}")));

        CustomerDTO dto = CustomerDTO.builder()
                .name("降级测试客户")
                .phone("13800009999")
                .email("fail@test.com")
                .idType("ID_CARD")
                .idNumber("FAILID001")
                .source(CustomerSource.WEBSITE)
                .status(CustomerStatus.ACTIVE)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        Long id = objectMapper.readTree(responseJson).path("data").path("id").asLong();

        mockMvc.perform(get("/api/customers/{id}/360", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.name").value("降级测试客户"))
                .andExpect(jsonPath("$.data.degraded").value(true));
    }

    @Test
    void getCustomer360_shouldReturn404ForNonExistent() throws Exception {
        mockMvc.perform(get("/api/customers/999999/360"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("404"));
    }
}
