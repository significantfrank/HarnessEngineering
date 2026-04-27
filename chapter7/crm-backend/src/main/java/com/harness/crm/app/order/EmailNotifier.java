package com.harness.crm.app.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class EmailNotifier {

    /**
     * 模拟邮件发送，随机失败
     */
    public void sendConfirmation(String email, String orderId) {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (ThreadLocalRandom.current().nextDouble() < 0.2) {
            throw new RuntimeException("邮件服务器连接超时: " + email);
        }
        log.info("邮件已发送至 {}，订单号: {}", email, orderId);
    }
}
