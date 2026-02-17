package com.assetcompass.tracker.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    // Use standard Java HttpClient (Java 11+)
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper(); // For JSON conversion

    // Helper method to send raw JSON to Brevo
    private void sendBrevoEmail(String toEmail, String subject, String htmlContent) {
        try {
            // 1. Build JSON Payload
            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", senderName, "email", senderEmail),
                    "to", List.of(Map.of("email", toEmail)),
                    "subject", subject,
                    "htmlContent", htmlContent
            );

            String jsonBody = objectMapper.writeValueAsString(payload);

            // 2. Build Request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key", brevoApiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // 3. Send Async
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            System.out.println("✅ Email sent successfully to " + toEmail);
                        } else {
                            System.err.println("❌ Brevo Error (" + response.statusCode() + "): " + response.body());
                        }
                    });

        } catch (Exception e) {
            System.err.println("❌ Failed to construct email request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 1. Send Verification Email
    public void sendVerificationEmail(String toEmail, String code) {
        String htmlContent = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                <h2 style="color: #2563eb;">Welcome to AssetCompass! 🧭</h2>
                <p>Your verification code is:</p>
                <h1 style="background-color: #f3f4f6; padding: 10px; display: inline-block; letter-spacing: 5px;">%s</h1>
                <p>This code expires in 15 minutes.</p>
            </body>
            </html>
            """, code);

        sendBrevoEmail(toEmail, "Verify your AssetCompass Account", htmlContent);
    }

    // 2. Send Trade Confirmation
    public void sendTradeConfirmation(String toEmail, String ticker, BigDecimal shares, BigDecimal price, BigDecimal totalSpent) {
        String htmlContent = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                <h2 style="color: #10b981;">Trade Executed 🚀</h2>
                <p>Bought <strong>%s</strong></p>
                <ul>
                    <li>Shares: %.4f</li>
                    <li>Price: $%.2f</li>
                    <li>Total: $%.2f</li>
                </ul>
            </body>
            </html>
            """, ticker, shares, price, totalSpent);

        sendBrevoEmail(toEmail, "Trade Executed: " + ticker, htmlContent);
    }
}