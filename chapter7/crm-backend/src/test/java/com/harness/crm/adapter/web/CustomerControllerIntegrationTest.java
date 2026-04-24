package com.harness.crm.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harness.crm.app.customer.dto.CustomerDTO;
import com.harness.crm.app.customer.dto.TagDTO;
import com.harness.crm.domain.customer.enums.CustomerLevel;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
import com.harness.crm.domain.customer.gateway.CustomerCenterGatewayI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

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

    @MockitoBean
    private CustomerCenterGatewayI customerCenterGateway;

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
                .idType("ID_CARD")
                .idNumber("110101199001011234")
                .build();
    }

    @Test
    void createCustomer_shouldReturnCreatedCustomer() throws Exception {
        CustomerDTO dto = CustomerDTO.builder()
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
                .idType("ID_CARD")
                .idNumber("CREATE_TEST_001")
                .build();

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("测试客户"))
                .andExpect(jsonPath("$.data.idType").value("ID_CARD"))
                .andExpect(jsonPath("$.data.idNumber").value("CREATE_TEST_001"))
                .andExpect(jsonPath("$.data.ccSyncStatus").value("SYNCED"))
                .andExpect(jsonPath("$.data.createTime").isNotEmpty());
    }

    @Test
    void updateCustomer_shouldReturnUpdatedCustomer() throws Exception {
        CustomerDTO createDto = CustomerDTO.builder()
                .name("更新前客户").idType("ID_CARD").idNumber("UPDATE_TEST_001").build();

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
                .idType("ID_CARD")
                .idNumber("UPDATE_TEST_001")
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
        CustomerDTO createDto = CustomerDTO.builder()
                .name("删除客户").idType("ID_CARD").idNumber("DELETE_TEST_001").build();

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
        CustomerDTO createDto = CustomerDTO.builder()
                .name("查找客户").source(CustomerSource.WEBSITE).level(CustomerLevel.VIP)
                .idType("ID_CARD").idNumber("FIND_TEST_001").build();

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
                .andExpect(jsonPath("$.data.name").value("查找客户"))
                .andExpect(jsonPath("$.data.source").value("WEBSITE"))
                .andExpect(jsonPath("$.data.level").value("VIP"));
    }

    @Test
    void listCustomers_shouldReturnPagedResult() throws Exception {
        CustomerDTO dto1 = CustomerDTO.builder().name("阿里云").source(CustomerSource.WEBSITE).level(CustomerLevel.VIP).idType("ID_CARD").idNumber("ALI001").build();
        CustomerDTO dto2 = CustomerDTO.builder().name("腾讯云").source(CustomerSource.REFERRAL).level(CustomerLevel.NORMAL).idType("ID_CARD").idNumber("TENCENT001").build();
        CustomerDTO dto3 = CustomerDTO.builder().name("阿里影业").source(CustomerSource.AD).level(CustomerLevel.VIP).idType("ID_CARD").idNumber("ALIFILM001").build();

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

    @Test
    void createCustomer_withTags_shouldReturnCustomerWithTags() throws Exception {
        MvcResult tagResult1 = mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TagDTO.builder().name("标签1").color("#FF0000").build())))
                .andExpect(status().isOk()).andReturn();
        Long tagId1 = objectMapper.readTree(tagResult1.getResponse().getContentAsString()).path("data").path("id").asLong();

        MvcResult tagResult2 = mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TagDTO.builder().name("标签2").color("#00FF00").build())))
                .andExpect(status().isOk()).andReturn();
        Long tagId2 = objectMapper.readTree(tagResult2.getResponse().getContentAsString()).path("data").path("id").asLong();

        CustomerDTO dto = CustomerDTO.builder()
                .name("带标签客户")
                .idType("ID_CARD")
                .idNumber("TAGCUST001")
                .tagIds(List.of(tagId1, tagId2))
                .build();

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.name").value("带标签客户"))
                .andExpect(jsonPath("$.data.tags").isArray())
                .andExpect(jsonPath("$.data.tags.length()").value(2));
    }

    @Test
    void updateCustomer_replaceTags_shouldReflectNewTags() throws Exception {
        MvcResult tagResult = mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TagDTO.builder().name("替换标签").color("#0000FF").build())))
                .andExpect(status().isOk()).andReturn();
        Long tagId = objectMapper.readTree(tagResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        CustomerDTO createDto = CustomerDTO.builder().name("替换标签客户").idType("ID_CARD").idNumber("REPLCUST001").build();
        MvcResult createResult = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andReturn();
        Long customerId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        CustomerDTO updateDto = CustomerDTO.builder()
                .name("替换标签客户")
                .status(CustomerStatus.ACTIVE)
                .idType("ID_CARD")
                .idNumber("REPLCUST001")
                .tagIds(List.of(tagId))
                .build();
        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tags.length()").value(1))
                .andExpect(jsonPath("$.data.tags[0].name").value("替换标签"));
    }

    @Test
    void listCustomers_filterByTags_shouldReturnMatchedCustomers() throws Exception {
        MvcResult t1 = mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TagDTO.builder().name("筛选标签A").color("#AA0000").build())))
                .andExpect(status().isOk()).andReturn();
        Long tagId1 = objectMapper.readTree(t1.getResponse().getContentAsString()).path("data").path("id").asLong();

        MvcResult t2 = mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TagDTO.builder().name("筛选标签B").color("#00AA00").build())))
                .andExpect(status().isOk()).andReturn();
        Long tagId2 = objectMapper.readTree(t2.getResponse().getContentAsString()).path("data").path("id").asLong();

        CustomerDTO dto = CustomerDTO.builder()
                .name("双标签客户")
                .idType("ID_CARD")
                .idNumber("DTAGCUST001")
                .tagIds(List.of(tagId1, tagId2))
                .build();
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        CustomerDTO dto2 = CustomerDTO.builder()
                .name("单标签客户")
                .idType("ID_CARD")
                .idNumber("STAGCUST001")
                .tagIds(List.of(tagId1))
                .build();
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/customers")
                        .param("tagIds", tagId1.toString(), tagId2.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("双标签客户"));
    }
}
