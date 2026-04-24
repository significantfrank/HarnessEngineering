package com.harness.crm.app.customer.dto;

import com.harness.crm.domain.customer.enums.CcSyncStatus;
import com.harness.crm.domain.customer.enums.CustomerLevel;
import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.customer.enums.CustomerStatus;
import com.harness.crm.app.customer.dto.TagDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private Long id;

    @NotBlank(message = "客户名称不能为空")
    private String name;

    private String phone;

    private String email;

    private String company;

    private String address;

    private CustomerSource source;

    private CustomerLevel level;

    private String industry;

    private String website;

    private String contactPerson;

    private LocalDateTime lastFollowUp;

    private CustomerStatus status;

    private String remark;

    private String idType;

    private String idNumber;

    private CcSyncStatus ccSyncStatus;

    private List<TagDTO> tags;

    private List<Long> tagIds;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
