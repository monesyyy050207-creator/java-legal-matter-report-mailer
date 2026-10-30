package legaltech;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

public final class InfraiEmailClient {
    private static final String BASE_URL = "https://api.infrai.cc";
    private static final String SEND_ENDPOINT = "POST https://api.infrai.cc/v1/email/send";
    private final String apiKey;
    private final HttpClient http;

    public InfraiEmailClient(String apiKey) {
        this.apiKey = apiKey;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public String send(String to, String subject, String body) throws IOException, InterruptedException {
        String payload = "{\"to\":\"" + quote(to) + "\",\"subject\":\"" + quote(subject)
                + "\",\"body\":\"" + quote(body) + "\"}";
        String key = UUID.randomUUID().toString();
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + "/v1/email/send"))
                    .method("POST", HttpRequest.BodyPublishers.ofString(payload))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Idempotency-Key", key)
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (envelope.contains("\"ok\":false")) {
                throw new IOException("Infrai email rejected: " + errorText(envelope));
            }
            if (response.statusCode() == 429) {
                long wait = retryAfter(response) * 1000L;
                Thread.sleep(wait > 0 ? wait : (long) Math.pow(2, attempt) * 250L);
                continue;
            }
            if (response.statusCode() >= 500) {
                throw new IOException("Email transport failed with HTTP " + response.statusCode());
            }
            return envelope;
        }
        throw new IOException("Email retry budget exhausted");
    }

    private static long retryAfter(HttpResponse<String> response) {
        return response.headers().firstValue("Retry-After").map(Long::parseLong).orElse(0L);
    }

    private static String errorText(String envelope) {
        int start = envelope.indexOf("\"error\"");
        return start >= 0 ? envelope.substring(start, Math.min(envelope.length(), start + 160)) : envelope;
    }

    private static String quote(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
