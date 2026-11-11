package com.example.fintech;

import java.math.BigDecimal;

public final class BalanceGuardianTest {
    public static void main(String[] args) {
        BalanceGuardian guardian = new BalanceGuardian();
        var low = guardian.decide(new BalanceGuardian.AccountSnapshot(
                new BigDecimal("4.00"), new BigDecimal("5.00"), new BigDecimal("20.00")));
        var healthy = guardian.decide(new BalanceGuardian.AccountSnapshot(
                new BigDecimal("8.00"), new BigDecimal("5.00"), new BigDecimal("20.00")));
        if (low.decision() != BalanceGuardian.Decision.RECHARGE ||
                low.amount().compareTo(new BigDecimal("20.00")) != 0 ||
                healthy.decision() != BalanceGuardian.Decision.WAIT) {
            throw new AssertionError("balance policy decision changed");
        }
        var data = InfraiClient.decodeResponse(200,
                "{\"ok\" : true, \"data\": {\"balance\": \"8.00\"}, \"metadata\": {}}");
        var liveDecision = TopupMigrationExample.printDecision(
                data.getAsJsonObject().get("balance").getAsBigDecimal());
        if (liveDecision.decision() != BalanceGuardian.Decision.WAIT) {
            throw new AssertionError("live balance was ignored");
        }
        try {
            InfraiClient.decodeResponse(400,
                    "{\"ok\":false,\"error\":{\"code\":\"REJECTED\"},\"data\":{\"ok\":true}}");
            throw new AssertionError("business rejection was ignored");
        } catch (IllegalStateException expected) {
            if (!expected.getMessage().contains("REJECTED")) throw expected;
        }
        System.out.println("BalanceGuardianTest passed");
    }
}
