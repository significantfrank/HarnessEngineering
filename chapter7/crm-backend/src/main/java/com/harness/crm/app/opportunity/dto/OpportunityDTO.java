package com.harness.crm.app.opportunity.dto;

import com.harness.crm.domain.opportunity.enums.OppStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityDTO {

    private Long id;

    @NotBlank(message = "机会标题不能为空")
    private String title;

    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    private BigDecimal amount;

    private OppStage stage;

    private Integer probability;

    private LocalDate expectedCloseDate;

    private Long leadId;

    private String ownerName;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
