package com.assetcompass.tracker.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // Inject the real JavaMailSender (configured in application.properties)
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 1. Verification Email (Gmail)
    public void sendVerificationEmail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("AssetCompass <noreply@assetcompass.com>");
            helper.setTo(toEmail);
            helper.setSubject("Verify your AssetCompass Account");

            String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <h2 style="color: #2563eb;">Welcome to AssetCompass! 🧭</h2>
                    <p>Please use the code below to verify your account:</p>
                    <h1 style="background-color: #f3f4f6; padding: 10px; display: inline-block; letter-spacing: 5px;">%s</h1>
                    <p>If you didn't request this, please ignore this email.</p>
                </div>
                """, code);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("✅ Verification email sent to " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }

    // 2. Trade Confirmation Email (The "Pro" Receipt)
    public void sendTradeConfirmation(String toEmail, String ticker, BigDecimal shares, BigDecimal price, BigDecimal totalSpent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("AssetCompass <noreply@assetcompass.com>");
            helper.setTo(toEmail);
            helper.setSubject("Trade Confirmation: You bought " + ticker);

            String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <h2 style="color: #10b981;">Trade Executed Successfully 🚀</h2>
                    <p>You have purchased shares of <strong>%s</strong>.</p>
                    <hr style="border: 0; border-top: 1px solid #eee;">
                    <p><strong>Shares Acquired:</strong> %.4f</p>
                    <p><strong>Price per Share:</strong> $%.2f</p>
                    <p><strong>Total Investment:</strong> $%.2f</p>
                    <hr style="border: 0; border-top: 1px solid #eee;">
                    <p style="font-size: 12px; color: #666;">This is an automated message from your WealthOS.</p>
                </div>
                """, ticker, shares, price, totalSpent);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("✅ Trade confirmation sent to " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Failed to send trade email: " + e.getMessage());
        }
    }
}