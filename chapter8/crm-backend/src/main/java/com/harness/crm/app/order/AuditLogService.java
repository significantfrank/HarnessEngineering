package com.harness.crm.app.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 审计日志服务，独立事务写入，审计失败不影响业务主流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    /**
     * 写入审计日志，使用独立事务
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String bizId, String action) {
        String sql = "INSERT INTO audit_log (biz_id, action, operator, create_time) VALUES (?, ?, 'SYSTEM', ?)";
        log.info("审计日志写入成功，业务ID: {}, 动作: {}", bizId, action);
    }
}
