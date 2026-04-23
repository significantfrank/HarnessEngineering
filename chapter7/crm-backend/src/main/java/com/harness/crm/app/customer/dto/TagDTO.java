package com.harness.crm.app.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {

    private Long id;

    private String name;

    private String color;

    private Long customerCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
