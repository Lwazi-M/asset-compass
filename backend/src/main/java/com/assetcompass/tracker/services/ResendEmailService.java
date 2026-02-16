package com.assetcompass.tracker.services;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;   // <--- UPDATED
import com.resend.services.emails.model.SendEmailResponse;  // <--- UPDATED
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResendEmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    public void sendVerificationEmail(String toEmail, String code) {
        Resend resend = new Resend(apiKey);

        String htmlContent = "<h1>Welcome to AssetCompass 🧭</h1>" +
                "<p>Your secure login code is:</p>" +
                "<h2 style='color: #2563eb; font-size: 24px; letter-spacing: 2px;'>" + code + "</h2>" +
                "<p>This code will expire in 10 minutes.</p>";

        // UPDATED: Using SendEmailRequest instead of CreateEmailOptions
        SendEmailRequest params = SendEmailRequest.builder()
                .from("AssetCompass <onboarding@resend.dev>")
                .to(toEmail)
                .subject("Your Login Code")
                .html(htmlContent)
                .build();

        try {
            // UPDATED: Using SendEmailResponse
            SendEmailResponse data = resend.emails().send(params);
            System.out.println("Email sent successfully! ID: " + data.getId());
        } catch (ResendException e) {
            e.printStackTrace();
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}