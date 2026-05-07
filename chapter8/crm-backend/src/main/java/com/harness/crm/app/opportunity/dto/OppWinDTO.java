package com.harness.crm.app.opportunity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OppWinDTO {

    @NotNull(message = "订单金额不能为空")
    private BigDecimal amount;

    private String remark;
}
