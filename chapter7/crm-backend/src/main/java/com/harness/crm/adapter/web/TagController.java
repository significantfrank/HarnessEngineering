package com.harness.crm.adapter.web;

import com.harness.crm.adapter.web.common.ApiResponse;
import com.harness.crm.app.customer.TagService;
import com.harness.crm.app.customer.dto.TagDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ApiResponse<TagDTO> create(@Valid @RequestBody TagDTO dto) {
        return ApiResponse.success(tagService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<TagDTO> update(@PathVariable Long id, @Valid @RequestBody TagDTO dto) {
        return ApiResponse.success(tagService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tagService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<List<TagDTO>> list() {
        return ApiResponse.success(tagService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<TagDTO> findById(@PathVariable Long id) {
        return ApiResponse.success(tagService.findById(id));
    }
}
