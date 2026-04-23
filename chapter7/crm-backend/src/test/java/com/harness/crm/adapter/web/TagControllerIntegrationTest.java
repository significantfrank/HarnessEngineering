package com.harness.crm.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harness.crm.app.customer.dto.TagDTO;
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
class TagControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** 创建标签 - Happy Case */
    @Test
    void createTag_shouldReturnCreatedTag() throws Exception {
        TagDTO dto = TagDTO.builder().name("大客户").color("#FF0000").build();

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("大客户"))
                .andExpect(jsonPath("$.data.color").value("#FF0000"))
                .andExpect(jsonPath("$.data.createTime").isNotEmpty());
    }

    /** 创建标签 - 名称重复 */
    @Test
    void createTag_duplicateName_shouldFail() throws Exception {
        TagDTO dto = TagDTO.builder().name("重复标签").color("#00FF00").build();
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    /** 更新标签 */
    @Test
    void updateTag_shouldReturnUpdatedTag() throws Exception {
        TagDTO createDto = TagDTO.builder().name("旧标签").color("#000000").build();
        MvcResult createResult = mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andReturn();
        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        TagDTO updateDto = TagDTO.builder().name("新标签").color("#FFFFFF").build();
        mockMvc.perform(put("/api/tags/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.name").value("新标签"))
                .andExpect(jsonPath("$.data.color").value("#FFFFFF"));
    }

    /** 删除未使用的标签 */
    @Test
    void deleteTag_unused_shouldSucceed() throws Exception {
        TagDTO dto = TagDTO.builder().name("可删除标签").color("#123456").build();
        MvcResult createResult = mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn();
        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(delete("/api/tags/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));
    }

    /** 删除使用中的标签应被拒绝 */
    @Test
    void deleteTag_inUse_shouldFail() throws Exception {
        // 创建标签
        TagDTO tagDto = TagDTO.builder().name("使用中标签").color("#AABBCC").build();
        MvcResult tagResult = mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagDto)))
                .andReturn();
        Long tagId = objectMapper.readTree(tagResult.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 创建带标签的客户
        String customerJson = "{\"name\":\"测试客户\",\"tagIds\":[" + tagId + "]}";
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson))
                .andExpect(status().isOk());

        // 删除标签应被拒绝
        mockMvc.perform(delete("/api/tags/{id}", tagId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    /** 查询标签列表 */
    @Test
    void listTags_shouldReturnAllTags() throws Exception {
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TagDTO.builder().name("标签A").color("#111111").build())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").isArray());
    }
}
