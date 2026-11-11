package com.example.fintech;

import java.math.BigDecimal;

public final class BalanceGuardian {
    public enum Decision { RECHARGE, WAIT }

    public record AccountSnapshot(BigDecimal balance, BigDecimal triggerBalance,
                                  BigDecimal rechargeAmount) {}

    public record RechargeDecision(Decision decision, BigDecimal amount) {}

    public RechargeDecision decide(AccountSnapshot snapshot) {
        if (snapshot.balance().compareTo(snapshot.triggerBalance()) <= 0) {
            return new RechargeDecision(Decision.RECHARGE, snapshot.rechargeAmount());
        }
        return new RechargeDecision(Decision.WAIT, BigDecimal.ZERO);
    }
}
