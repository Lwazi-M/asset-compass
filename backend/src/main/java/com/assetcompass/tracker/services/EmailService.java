package com.assetcompass.tracker.services;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendVerificationEmail(String toEmail, String code) {
        // In the future, we will use JavaMailSender here.
        // For now, we print to the console so you can see it.
        System.out.println("==================================================");
        System.out.println("📨 SENDING EMAIL TO: " + toEmail);
        System.out.println("🔑 YOUR VERIFICATION CODE IS: " + code);
        System.out.println("==================================================");
    }
}