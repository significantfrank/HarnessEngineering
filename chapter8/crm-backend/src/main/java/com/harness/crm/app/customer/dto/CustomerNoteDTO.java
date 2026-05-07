package com.harness.crm.app.customer.dto;

import com.harness.crm.domain.customer.enums.NoteCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerNoteDTO {

    private Long id;

    private Long customerId;

    @NotNull(message = "小记类型不能为空")
    private NoteCategory category;

    @NotBlank(message = "小记内容不能为空")
    private String content;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
