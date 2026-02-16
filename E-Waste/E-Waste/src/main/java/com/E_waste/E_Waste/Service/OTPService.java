package com.E_waste.E_Waste.Service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class OTPService {

    // Store OTP with timestamp: key -> {otp, timestamp}
    private static class OTPData {
        String otp;
        LocalDateTime timestamp;

        OTPData(String otp) {
            this.otp = otp;
            this.timestamp = LocalDateTime.now();
        }
    }

    private final Map<String, OTPData> otpStore = new ConcurrentHashMap<>();
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int OTP_LENGTH = 6;

    /**
     * Generate a 6-digit OTP and store it with timestamp
     */
    public String generateOTP(String email) {
        // Generate 6-digit OTP
        Random random = new Random();
        String otp = String.format("%06d", random.nextInt(1000000));

        // Debug: Print OTP to console
        System.out.println("========================================");
        System.out.println("OTP GENERATED:");
        System.out.println("Email: " + email);
        System.out.println("OTP: " + otp);
        System.out.println("========================================");

        // Store with timestamp
        otpStore.put(email, new OTPData(otp));

        // Clean up expired OTPs
        cleanupExpiredOTPs();

        return otp;
    }

    /**
     * Verify OTP - checks if OTP exists, matches, and hasn't expired
     * @param removeAfterVerification - if true, removes OTP after verification (one-time use)
     */
    public boolean verifyOTP(String email, String enteredOTP, boolean removeAfterVerification) {
        OTPData otpData = otpStore.get(email);

        if (otpData == null) {
            return false;
        }

        // Check if expired
        long minutesElapsed = ChronoUnit.MINUTES.between(otpData.timestamp, LocalDateTime.now());
        if (minutesElapsed > OTP_EXPIRY_MINUTES) {
            otpStore.remove(email); // Remove expired OTP
            return false;
        }

        // Check if OTP matches
        boolean isValid = otpData.otp.equals(enteredOTP);

        // Remove OTP after use (one-time use) - only if requested
        if (isValid && removeAfterVerification) {
            otpStore.remove(email);
        }

        return isValid;
    }

    /**
     * Verify OTP - checks if OTP exists, matches, and hasn't expired
     * Default: removes OTP after verification (one-time use)
     */
    public boolean verifyOTP(String email, String enteredOTP) {
        return verifyOTP(email, enteredOTP, true);
    }

    /**
     * Check if OTP exists for email (not expired)
     */
    public boolean hasOTP(String email) {
        OTPData otpData = otpStore.get(email);
        if (otpData == null) {
            return false;
        }

        long minutesElapsed = ChronoUnit.MINUTES.between(otpData.timestamp, LocalDateTime.now());
        if (minutesElapsed > OTP_EXPIRY_MINUTES) {
            otpStore.remove(email);
            return false;
        }

        return true;
    }

    /**
     * Remove expired OTPs from store
     */
    private void cleanupExpiredOTPs() {
        LocalDateTime now = LocalDateTime.now();
        otpStore.entrySet().removeIf(entry -> {
            long minutesElapsed = ChronoUnit.MINUTES.between(entry.getValue().timestamp, now);
            return minutesElapsed > OTP_EXPIRY_MINUTES;
        });
    }

    /**
     * Get remaining time in seconds for OTP
     */
    public long getRemainingTime(String email) {
        OTPData otpData = otpStore.get(email);
        if (otpData == null) {
            return 0;
        }

        long minutesElapsed = ChronoUnit.MINUTES.between(otpData.timestamp, LocalDateTime.now());
        long remainingMinutes = OTP_EXPIRY_MINUTES - minutesElapsed;

        return Math.max(0, remainingMinutes * 60); // Return in seconds
    }
}

