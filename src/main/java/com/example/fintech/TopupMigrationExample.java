package com.example.fintech;

import java.math.BigDecimal;

public final class TopupMigrationExample {
    public static void main(String[] args) throws Exception {
        if (args.length == 0 || !args[0].equals("--live")) {
            printDecision(new BigDecimal("4.00"));
            return;
        }

        if (args.length != 2 || args[1].isBlank()) {
            throw new IllegalArgumentException("Usage: TopupMigrationExample --live <recipient>");
        }
        String recipient = args[1];
        if (recipient.chars().anyMatch(c -> c < 32 || c == '"' || c == '\\')) {
            throw new IllegalArgumentException("Recipient contains invalid characters");
        }

        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("Set INFRAI_API_KEY first");
        InfraiClient infrai = new InfraiClient(key);
        var decision = printDecision(infrai.readBalance());
        if (decision.decision() == BalanceGuardian.Decision.RECHARGE) {
            infrai.configureAutorecharge(new InfraiClient.BigDecimalText("5.00", "20.00"));
            var message = infrai.sendNotification(recipient, "Balance recharge event",
                    "Recharge decision accepted for 20.00.");
            System.out.println("Notification data: " + message);
        }
    }

    static BalanceGuardian.RechargeDecision printDecision(BigDecimal balance) {
        var decision = new BalanceGuardian().decide(new BalanceGuardian.AccountSnapshot(
                balance, new BigDecimal("5.00"), new BigDecimal("20.00")));
        System.out.println("Decision: " + decision.decision() + " amount=" + decision.amount());
        System.out.println("Cutover: balance read -> recharge rule -> notification -> ownership handoff");
        return decision;
    }
}
