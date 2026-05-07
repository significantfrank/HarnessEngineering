package com.harness.crm.domain.customer.entity;

import com.harness.crm.domain.customer.enums.NoteCategory;
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
@Table(name = "customer_note")
public class CustomerNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoteCategory category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(nullable = false)
    private LocalDateTime updateTime;

    /**
     * 新增小记时，联动更新客户的最后跟进时间
     * 当小记创建时间晚于客户当前lastFollowUp时，将lastFollowUp更新为小记创建时间
     */
    public void updateCustomerLastFollowUp(CustomerEntity customer) {
        if (customer.getLastFollowUp() == null || this.createTime.isAfter(customer.getLastFollowUp())) {
            customer.setLastFollowUp(this.createTime);
        }
    }

    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createTime == null) {
            createTime = now;
        }
        updateTime = now;
    }
}
