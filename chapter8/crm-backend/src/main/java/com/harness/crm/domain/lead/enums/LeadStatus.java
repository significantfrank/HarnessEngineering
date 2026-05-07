package com.harness.crm.domain.lead.enums;

public enum LeadStatus {
    NEW,
    CONTACTED,
    QUALIFIED,
    UNQUALIFIED,
    CONVERTED;

    /**
     * 是否为终态
     */
    public boolean isTerminal() {
        return this == CONVERTED || this == UNQUALIFIED;
    }
}
