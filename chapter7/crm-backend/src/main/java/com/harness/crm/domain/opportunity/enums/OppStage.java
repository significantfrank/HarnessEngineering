package com.harness.crm.domain.opportunity.enums;

public enum OppStage {
    PROSPECTING,
    QUALIFYING,
    PROPOSAL,
    NEGOTIATION,
    WON,
    LOST;

    /**
     * 是否为终态
     */
    public boolean isTerminal() {
        return this == WON || this == LOST;
    }
}
