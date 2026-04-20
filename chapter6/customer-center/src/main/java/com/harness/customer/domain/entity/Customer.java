package com.harness.customer.domain.entity;

import com.harness.customer.infrastructure.converter.HoldingProductListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private IdType idType;

    @Column(nullable = false, length = 64, unique = true)
    private String idNumber;

    @Column(length = 20)
    private String phone;

    @Column(length = 128)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Gender gender;

    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AccountStatus accountStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private MemberLevel memberLevel;

    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private AuthLevel authLevel;

    @Column(length = 512)
    private String idPhotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private RiskProfile riskProfile;

    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private OccupationCategory occupation;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private IncomeRange incomeRange;

    @Column(precision = 18, scale = 2)
    private BigDecimal aum;

    @Column(precision = 18, scale = 2)
    private BigDecimal availableBalance;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalReturn;

    @Convert(converter = HoldingProductListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<HoldingProduct> holdingProducts = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

    public void activateAccount() {
        if (this.accountStatus == AccountStatus.FROZEN) {
            throw new IllegalStateException("Frozen account cannot be activated directly, please unfreeze first");
        }
        this.accountStatus = AccountStatus.NORMAL;
    }

    public void freezeAccount() {
        this.accountStatus = AccountStatus.FROZEN;
    }

    public void cancelAccount() {
        this.accountStatus = AccountStatus.CANCELLED;
    }

    public void upgradeMember(MemberLevel newLevel) {
        if (newLevel.ordinal() <= this.memberLevel.ordinal()) {
            throw new IllegalArgumentException("Cannot downgrade member level");
        }
        this.memberLevel = newLevel;
    }

    public void upgradeAuth(AuthLevel newLevel) {
        if (newLevel.ordinal() <= this.authLevel.ordinal()) {
            throw new IllegalArgumentException("Cannot downgrade auth level");
        }
        this.authLevel = newLevel;
    }

    public void updateRiskProfile(RiskProfile riskProfile) {
        this.riskProfile = riskProfile;
    }
}
