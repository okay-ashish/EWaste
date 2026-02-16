package com.E_waste.E_Waste.Service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * Send OTP via email
     */
    public boolean sendOTPEmail(String toEmail, String otp) {
        // If mail sender is not configured, log to console
        if (mailSender == null) {
            System.out.println("========================================");
            System.out.println("⚠️  EMAIL SERVICE NOT CONFIGURED");
            System.out.println("To: " + toEmail);
            System.out.println("OTP: " + otp);
            System.out.println("========================================");
            System.out.println("To enable email sending, configure SMTP in application.properties");
            return true; // Return true for development
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("EWaste - OTP Verification Code");
            message.setText(
                    "Hello,\n\n" +
                            "Your OTP for Ewaste Management registration is:\n\n" +
                            "    " + otp + "\n\n" +
                            "This OTP is valid for 5 minutes.\n\n" +
                            "If you didn't request this OTP, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "EWaste Management Team"
            );

            // Use configured email or default
            if (fromEmail != null && !fromEmail.isEmpty()) {
                message.setFrom(fromEmail);
            } else {
                message.setFrom("noreply@campuslink.com");
            }

            mailSender.send(message);

            System.out.println("✅ Email sent successfully to: " + toEmail);
            System.out.println("   OTP: " + otp);

            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + toEmail);
            System.err.println("   Error: " + e.getMessage());
            e.printStackTrace();

            // Fallback: Print to console for development
            System.out.println("========================================");
            System.out.println("EMAIL OTP (Email sending failed, using console):");
            System.out.println("To: " + toEmail);
            System.out.println("OTP: " + otp);
            System.out.println("========================================");

            return true; // Return true for development
        }
    }
}


