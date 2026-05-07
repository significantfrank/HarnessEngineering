package com.harness.crm.app.lead.dto;

import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.lead.enums.LeadStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadDTO {

    private Long id;

    @NotBlank(message = "线索名称不能为空")
    private String name;

    private String phone;

    private String email;

    private String company;

    private CustomerSource source;

    private LeadStatus status;

    private Long customerId;

    private String ownerName;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
