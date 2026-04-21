package com.harness.customer.adapter.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.harness.customer.domain.entity.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private Long id;

    @NotBlank
    @Size(max = 64)
    private String name;

    @NotNull
    private IdType idType;

    @NotBlank
    @Size(max = 64)
    private String idNumber;

    @Size(max = 20)
    private String phone;

    @Email
    @Size(max = 128)
    private String email;

    private Gender gender;

    private LocalDate birthday;

    private AccountStatus accountStatus;

    private MemberLevel memberLevel;

    private AuthLevel authLevel;

    private String idPhotoUrl;

    private RiskProfile riskProfile;

    private OccupationCategory occupation;

    private IncomeRange incomeRange;

    private BigDecimal aum;

    private BigDecimal availableBalance;

    private BigDecimal totalReturn;

    private FreezeReason freezeReason;

    private String unfreezeReason;

    private List<HoldingProductDTO> holdingProducts;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoldingProductDTO {
        private ProductType productType;
        private String productCode;
        private String productName;
        private BigDecimal amount;
    }
}
