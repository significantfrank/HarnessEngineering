package com.harness.crm.domain.order.enums;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    COMPLETED,
    CANCELLED;

    /**
     * 是否为终态
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }
}
