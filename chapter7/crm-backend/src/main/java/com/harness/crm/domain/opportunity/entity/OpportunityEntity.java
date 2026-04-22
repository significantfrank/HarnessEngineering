package com.harness.crm.domain.opportunity.entity;

import com.harness.crm.domain.opportunity.enums.OppStage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "opportunity")
public class OpportunityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private Long customerId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OppStage stage;

    private Integer probability;

    private LocalDate expectedCloseDate;

    private Long leadId;

    @Column(length = 100)
    private String ownerName;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(nullable = false)
    private LocalDateTime updateTime;

    /**
     * 创建前初始化默认值
     */
    public void prePersist() {
        if (stage == null) {
            stage = OppStage.PROSPECTING;
        }
        LocalDateTime now = LocalDateTime.now();
        if (createTime == null) {
            createTime = now;
        }
        updateTime = now;
    }

    /**
     * 更新前刷新时间
     */
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

    /**
     * 是否处于终态
     */
    public boolean isTerminal() {
        return stage != null && stage.isTerminal();
    }
}
