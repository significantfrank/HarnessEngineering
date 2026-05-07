package com.harness.customer.adapter.web;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnfreezeRequestDTO {

    @NotBlank
    private String reason;

    private String authorizationDocument;
}
