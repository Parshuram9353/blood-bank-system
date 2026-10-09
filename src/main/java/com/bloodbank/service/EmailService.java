package com.bloodbank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        log.info("--- SIMULATED EMAIL NOTIFICATION ---");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Body: {}", body);
        log.info("------------------------------------");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@bloodbank.org");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.warn("Failed to send actual email via SMTP (using logger fallback): {}", e.getMessage());
        }
    }

    public void sendLowStockAlert(String bloodGroup, int currentStock) {
        String subject = "Low Stock Alert: Blood Group " + bloodGroup;
        String body = String.format("Dear Staff,\n\nThis is an automated alert indicating that the stock for blood group %s has fallen below the threshold. Current available stock: %d units.\n\nPlease arrange for donations or request transfers.\n\nBest regards,\nBlood Bank Management System",
                bloodGroup, currentStock);
        sendEmail("staff@bloodbank.org", subject, body);
    }

    public void sendBloodExpiryAlert(String bloodGroup, String batchId, int units, String expiryDate) {
        String subject = "Upcoming Blood Expiry Alert: Blood Group " + bloodGroup;
        String body = String.format("Dear Staff,\n\nThis is an automated alert indicating that batch #%s of blood group %s (%d units) is expiring on %s.\n\nPlease prioritize using these units or remove them if expired.\n\nBest regards,\nBlood Bank Management System",
                batchId, bloodGroup, units, expiryDate);
        sendEmail("staff@bloodbank.org", subject, body);
    }

    public void sendRequestStatusAlert(String recipientEmail, String patientName, String bloodGroup, String status) {
        String subject = "Blood Request Status Update: " + status;
        String body = String.format("Dear User,\n\nYour blood request for patient %s (Blood Group %s) has been updated.\n\nNew Status: %s\n\nThank you,\nBlood Bank Management System",
                patientName, bloodGroup, status);
        sendEmail(recipientEmail, subject, body);
    }
}
