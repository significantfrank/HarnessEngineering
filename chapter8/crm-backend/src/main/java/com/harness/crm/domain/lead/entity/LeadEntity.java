package com.harness.crm.domain.lead.entity;

import com.harness.crm.domain.customer.enums.CustomerSource;
import com.harness.crm.domain.lead.enums.LeadStatus;
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

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lead")
public class LeadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 200)
    private String company;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private CustomerSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeadStatus status;

    private Long customerId;

    @Column(length = 20)
    private String idType;

    @Column(length = 50)
    private String idNumber;

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
        if (status == null) {
            status = LeadStatus.NEW;
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
        return status != null && status.isTerminal();
    }
}
