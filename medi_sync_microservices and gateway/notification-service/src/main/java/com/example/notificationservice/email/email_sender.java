package com.example.notificationservice.email;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Component
public class email_sender {
    private static final Logger log = LoggerFactory.getLogger(email_sender.class);
    private final JavaMailSender mailSender;

    public email_sender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String toEmail, String messageBody) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your Appointment Update");
            message.setText(messageBody);
            mailSender.send(message);
            log.info("Email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}
