package com.harness.crm.app.lead.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadConvertDTO {

    @NotBlank(message = "机会标题不能为空")
    private String opportunityTitle;

    @NotBlank(message = "证件类型不能为空")
    private String idType;

    @NotBlank(message = "证件号码不能为空")
    private String idNumber;

    private BigDecimal amount;

    private LocalDate expectedCloseDate;
}
