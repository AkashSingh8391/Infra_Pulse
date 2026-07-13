package com.infrapulse.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public void sendPasswordResetEmail(String toEmail, String name, String token) {
        String resetLink = frontendBaseUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Reset your InfraPulse password");
        message.setText("""
                Hi %s,

                We received a request to reset your InfraPulse password. Click the link below to set a new one:

                %s

                This link expires in 1 hour. If you didn't request this, you can safely ignore this email.

                - InfraPulse
                """.formatted(name, resetLink));

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            // Don't let a mail-provider hiccup break the forgot-password flow for the caller;
            // log it so it's visible in Render/Railway logs.
            log.warn("Failed to send password reset email to {}: {}", toEmail, ex.getMessage());
        }
    }
}
