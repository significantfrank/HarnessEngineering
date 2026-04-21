package com.harness.customer.adapter.web;

import com.harness.customer.domain.entity.FreezeReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FreezeRequestDTO {

    @NotNull
    private FreezeReason reason;
}
