package com.harness.crm.app.order;

import com.harness.crm.app.order.dto.LegacyOrderDTO;
import com.harness.crm.app.order.dto.OrderResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class LegacyOrderService {

    // 💡 遗留特征：静态缓存，缺乏失效机制，容易导致折扣计算规则滞后
    private static final Map<String, BigDecimal> DISCOUNT_RULE_CACHE = new ConcurrentHashMap<>();
    private static boolean RISK_SYSTEM_DEGRADED = false;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EmailNotifier emailNotifier;

    /**
     * 核心金融订单处理流程
     */
    public LegacyOrderDTO processOrder(LegacyOrderDTO req) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            validateRequest(req);

            boolean isEligible = checkCompliance(conn, req.getCustomerId(), req.getProductType());
            if (!isEligible) {
                log.warn("客户 {} 未通过合规校验，拒绝下单", req.getCustomerId());
                throw new RuntimeException("客户未通过" + req.getCustomerId() + "合规校验，拒绝下单");
            }

            BigDecimal discount = calculateFinancialDiscount(conn, req);

            String orderId = generateLegacyOrderNo();
            saveOrderToDb(conn, orderId, req, discount);

            handleRiskSync(orderId, req);

            insertAuditLog(conn, orderId, "FINANCIAL_TRANSACTION_CREATED");

            conn.commit();

            sendEmailNotification(req, orderId);

            req.setTotalAmount(req.getTotalAmount().subtract(discount));
            return req;

        } catch (Exception e) {
            rollbackQuietly(conn);
            log.error("金融订单处理异常", e);
            throw new RuntimeException("交易失败，请联系客户经理");
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * 基础校验（硬编码）
     */
    private void validateRequest(LegacyOrderDTO req) {
        if (req == null || req.getCustomerId() == null || req.getTotalAmount() == null) {
            throw new IllegalArgumentException("请求参数缺失");
        }
    }

    /**
     * 合规校验：JDBC 直查 customer 表，硬编码规则
     * 💡 痛点：合规逻辑与业务逻辑高度耦合，且缺乏灵活性
     */
    boolean checkCompliance(Connection conn, Long customerId, String productType) {
        String sql = "SELECT level, status FROM customer WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return false;
            }
            String level = rs.getString("level");
            String status = rs.getString("status");

            // 💡 痛点：硬编码合规规则，无法动态调整
            if ("INACTIVE".equals(status) || "LOST".equals(status)) {
                return false;
            }
            if (("NORMAL".equals(level) || "POTENTIAL".equals(level))
                    && ("FUND".equals(productType) || "BOND".equals(productType))) {
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("合规查询异常，默认拒绝", e);
            return false;
        }
    }

    /**
     * 折扣计算：先查静态缓存，miss 时 JDBC 查数据库
     * 💡 痛点：缓存永不过期，数据库中折扣规则已更新但缓存仍返回旧值
     */
    BigDecimal calculateFinancialDiscount(Connection conn, LegacyOrderDTO req) {
        String cacheKey = req.getCustomerLevel() + ":" + req.getProductType();
        BigDecimal cachedRate = DISCOUNT_RULE_CACHE.get(cacheKey);
        if (cachedRate != null) {
            log.info("命中折扣缓存: key={}, rate={}", cacheKey, cachedRate);
            return req.getTotalAmount().multiply(cachedRate);
        }

        BigDecimal rate = loadDiscountRateFromDb(conn, req.getCustomerLevel(), req.getProductType());

        // 💡 兜底硬编码：VIP大额订单 5% 折扣
        if (rate.equals(BigDecimal.ZERO)
                && "VIP".equals(req.getCustomerLevel())
                && req.getTotalAmount().compareTo(new BigDecimal("1000000")) > 0) {
            rate = new BigDecimal("0.05");
        }

        DISCOUNT_RULE_CACHE.put(cacheKey, rate);
        return req.getTotalAmount().multiply(rate);
    }

    /**
     * 从数据库加载折扣率
     * 💡 痛点：手动拼SQL取数，Service里混杂数据访问逻辑
     */
    private BigDecimal loadDiscountRateFromDb(Connection conn, String customerLevel, String productType) {
        String sql = "SELECT discount_rate FROM discount_rule WHERE customer_level = ? AND product_type = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerLevel);
            ps.setString(2, productType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("discount_rate");
            }
        } catch (Exception e) {
            log.warn("折扣规则查询失败，使用硬编码兜底", e);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 落盘订单：Raw JDBC INSERT
     * 💡 痛点：无ORM，字段映射脆弱，orderNo生成策略简陋（并发会重复）
     */
    private void saveOrderToDb(Connection conn, String orderId, LegacyOrderDTO req, BigDecimal discount) {
        String sql = "INSERT INTO `order` (order_no, customer_id, opportunity_id, total_amount, status, owner_name, remark, create_time, update_time) "
                + "VALUES (?, ?, ?, ?, 'PENDING', ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            ps.setLong(2, req.getCustomerId());
            ps.setLong(3, req.getOpportunityId());
            ps.setBigDecimal(4, req.getTotalAmount().subtract(discount));
            ps.setString(5, req.getOwnerName());
            ps.setString(6, req.getRemark());
            ps.setObject(7, LocalDateTime.now());
            ps.setObject(8, LocalDateTime.now());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("订单落库失败", e);
        }
    }

    /**
     * 生成遗留订单号
     * 💡 痛点：基于时间戳，高并发下可能重复
     */
    private String generateLegacyOrderNo() {
        return "FIN-ORD-" + System.currentTimeMillis();
    }

    /**
     * 风控系统同步：阻塞重试 + 失败放行
     */
    private void handleRiskSync(String orderId, LegacyOrderDTO req) {
        try {
            syncToRiskSystemBlocking(orderId, req);
        } catch (Exception e) {
            log.warn("风控系统同步首次失败，等待重试...", e);
            retryRiskSync(orderId, req);
        }
    }

    /**
     * 风控系统重试
     */
    private void retryRiskSync(String orderId, LegacyOrderDTO req) {
        try {
            Thread.sleep(2000); // 💡 阻塞重试
            syncToRiskSystemBlocking(orderId, req);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("风控重试被中断", e);
        } catch (Exception retryEx) {
            RISK_SYSTEM_DEGRADED = true;
            // 💡 隐患：风控失败竟然允许下单继续！这在金融场景是重大合规隐患，
            // 但在 Brownfield 系统中，为了"可用性"往往存在这种妥协。
            log.error("风控系统熔断，根据当前策略跳过强制拦截！", retryEx);
        }
    }

    /**
     * 模拟同步调用风控/反洗钱系统
     * 💡 痛点：同步阻塞，随机失败模拟外部系统不稳定
     */
    void syncToRiskSystemBlocking(String orderId, LegacyOrderDTO req) {
        try {
            Thread.sleep(500); // 💡 模拟网络耗时
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (ThreadLocalRandom.current().nextDouble() < 0.4) {
            throw new RuntimeException("风控系统返回异常，订单: " + orderId);
        }
        log.info("风控系统同步成功，订单: {}", orderId);
    }

    /**
     * 审计日志：Raw JDBC INSERT，与业务事务耦合
     * 💡 痛点：审计日志失败会导致整个订单事务回滚
     */
    void insertAuditLog(Connection conn, String orderId, String action) {
        String sql = "INSERT INTO audit_log (biz_id, action, operator, create_time) VALUES (?, ?, 'SYSTEM', ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            ps.setString(2, action);
            ps.setObject(3, LocalDateTime.now());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("审计日志写入失败", e);
        }
    }

    /**
     * 发送邮件通知（失败不影响主流程）
     */
    private void sendEmailNotification(LegacyOrderDTO req, String orderId) {
        try {
            emailNotifier.sendConfirmation(req.getEmail(), orderId);
        } catch (Exception mailEx) {
            log.warn("邮件通知发送失败", mailEx);
        }
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (Exception ignored) {
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception ignored) {
            }
        }
    }
}
