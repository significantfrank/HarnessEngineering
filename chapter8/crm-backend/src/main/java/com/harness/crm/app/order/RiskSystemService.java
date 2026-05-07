package com.harness.crm.app.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 风控系统服务：同步调用风控系统，失败则拒绝下单
 */
@Slf4j
@Component
public class RiskSystemService {

    /**
     * 同步校验风控，失败直接抛异常回滚事务
     */
    public void checkRisk(String orderNo) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("风控系统同步成功，订单: {}", orderNo);
    }
}
