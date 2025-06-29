package com.pratham.finvera.service;

import com.pratham.finvera.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    public void sendEmail(String toEmail, String subject, String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);

        String htmlBody = templateEngine.process(templateName, context);
        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    // Specific method for OTP (calls the general one)
    public void sendOtpEmail(String toEmail, String otp, String name) {
        sendEmail(
                toEmail,
                "Your OTP for Finvera Verification",
                "otp-verification.html",
                Map.of("otp", otp, "name", name));
    }

    public void sendWelcomeEmail(String toEmail, String name) {
        sendEmail(
                toEmail,
                "Welcome to Finvera 🎉",
                "welcome.html",
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
