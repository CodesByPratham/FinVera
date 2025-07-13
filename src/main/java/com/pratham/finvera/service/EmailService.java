package com.pratham.finvera.service;

import com.pratham.finvera.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    // General purpose method to send templated emails
    @Async
    public void sendEmail(String toEmail, String subject, String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);

        String htmlBody = templateEngine.process(templateName, context);
        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    // Specific method for OTP (calls the general one)
    @Async
    public void sendOtpEmail(String toEmail, String otp, String name) {
        Context context = new Context();

        context.setVariable("name", name);
        context.setVariable("otp", otp);

        String htmlContent = templateEngine.process("otp-verification", context);
        sendHtmlEmail(toEmail, "OTP Verification", htmlContent);
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String name) {
        sendEmail(
                toEmail,
                "Welcome to Finvera 🎉",
                "welcome.html",
                Map.of("name", name));
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String name) {
        sendEmail(
                toEmail,
                "Password Reset Successful",
                "password-reset-success.html",
                Map.of("name", name));
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendException("Failed to send email to " + to + ": " + e.getMessage());
        }
    }
}