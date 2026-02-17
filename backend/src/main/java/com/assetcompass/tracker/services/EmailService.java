package com.assetcompass.tracker.services;

import brevo.ApiClient;
import brevo.ApiException;
import brevo.Configuration;
import brevo.auth.ApiKeyAuth;
import brevo.model.*;
import com.getbrevo.api.TransactionalEmailsApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    // Helper to configure the Brevo Client
    private TransactionalEmailsApi getApiInstance() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        // Configure API key authorization: api-key
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey(brevoApiKey);
        return new TransactionalEmailsApi();
    }

    // 1. Send Verification Email
    public void sendVerificationEmail(String toEmail, String code) {
        try {
            TransactionalEmailsApi api = getApiInstance();
            SendSmtpEmail email = new SendSmtpEmail();

            email.setSender(new SendSmtpEmailSender().email(senderEmail).name(senderName));
            email.setTo(List.of(new SendSmtpEmailTo().email(toEmail)));
            email.setSubject("Verify your AssetCompass Account");

            String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <h2 style="color: #2563eb;">Welcome to AssetCompass! 🧭</h2>
                    <p>Your verification code is:</p>
                    <h1 style="background-color: #f3f4f6; padding: 10px; display: inline-block; letter-spacing: 5px;">%s</h1>
                    <p>This code expires in 15 minutes.</p>
                </div>
                """, code);
            email.setHtmlContent(htmlContent);

            api.sendTransacEmail(email);
            System.out.println("✅ Verification email sent to " + toEmail + " via Brevo API");

        } catch (ApiException e) {
            System.err.println("❌ Failed to send Brevo email: " + e.getResponseBody());
            e.printStackTrace();
        }
    }

    // 2. Send Trade Confirmation
    public void sendTradeConfirmation(String toEmail, String ticker, BigDecimal shares, BigDecimal price, BigDecimal totalSpent) {
        try {
            TransactionalEmailsApi api = getApiInstance();
            SendSmtpEmail email = new SendSmtpEmail();

            email.setSender(new SendSmtpEmailSender().email(senderEmail).name(senderName));
            email.setTo(List.of(new SendSmtpEmailTo().email(toEmail)));
            email.setSubject("Trade Executed: " + ticker);

            String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <h2 style="color: #10b981;">Trade Executed 🚀</h2>
                    <p>Bought <strong>%s</strong></p>
                    <ul>
                        <li>Shares: %.4f</li>
                        <li>Price: $%.2f</li>
                        <li>Total: $%.2f</li>
                    </ul>
                </div>
                """, ticker, shares, price, totalSpent);
            email.setHtmlContent(htmlContent);

            api.sendTransacEmail(email);
            System.out.println("✅ Trade email sent to " + toEmail + " via Brevo API");

        } catch (ApiException e) {
            System.err.println("❌ Failed to send trade email: " + e.getResponseBody());
        }
    }
}