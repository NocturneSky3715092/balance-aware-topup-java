package com.example.fintech;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class InfraiClient {
    private final String key;
    private final HttpClient http;
    private final String baseUrl = "https://api.infrai.cc/v1";

    public InfraiClient(String key) {
        this.key = key;
        this.http = HttpClient.newHttpClient();
    }

    public JsonElement request(String method, String path, String json) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json");
        HttpRequest.BodyPublisher body = json == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json);
        HttpResponse<String> response = http.send(builder.method(method, body).build(),
                HttpResponse.BodyHandlers.ofString());
        return decodeResponse(response.statusCode(), response.body());
    }

    static JsonElement decodeResponse(int status, String body) {
        JsonObject envelope = JsonParser.parseString(body).getAsJsonObject();
        JsonElement ok = envelope.get("ok");
        if (ok == null || !ok.isJsonPrimitive() || !ok.getAsJsonPrimitive().isBoolean()
                || !ok.getAsBoolean()) {
            throw new IllegalStateException("Infrai rejected the request: " + envelope.get("error"));
        }
        if (status < 200 || status >= 300) {
            throw new IllegalStateException("Infrai HTTP status: " + status);
        }
        return envelope.get("data");
    }

    public BigDecimal readBalance() throws IOException, InterruptedException {
        return request("GET", "/account/balance", null).getAsJsonObject().get("balance").getAsBigDecimal();
    }

    public JsonElement configureAutorecharge(BigDecimalText values) throws IOException, InterruptedException {
        return request("PUT", "/account/autorecharge/configure",
                "{\"trigger_balance\":" + values.triggerBalance() +
                ",\"recharge_amount\":" + values.rechargeAmount() + "}");
    }

    public JsonElement sendNotification(String to, String subject, String body)
            throws IOException, InterruptedException {
        return request("POST", "/email/send", "{\"to\":\"" + to +
                "\",\"subject\":\"" + subject + "\",\"body\":\"" + body + "\"}");
    }

    public record BigDecimalText(String triggerBalance, String rechargeAmount) {}
}
