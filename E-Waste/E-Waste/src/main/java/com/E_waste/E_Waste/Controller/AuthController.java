package com.E_waste.E_Waste.Controller;

import com.E_waste.E_Waste.Entity.User;
import com.E_waste.E_Waste.Service.EmailService;
import com.E_waste.E_Waste.Service.OTPService;
import com.E_waste.E_Waste.dto.*;
import com.E_waste.E_Waste.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OTPService otpService;

    /* ========== REGISTER ========== */

    @PostMapping("/register/send-otp")
    public ResponseEntity<?> sendRegisterOtp(@RequestBody Map<String, String> request) {
        return authService.sendRegisterOtp(request);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody AuthRequest request,
            @RequestParam String otp) {
        return authService.register(request, otp);
    }

    /* ========== LOGIN ========== */

    @PostMapping("/login/send-otp")
    public ResponseEntity<?> sendLoginOtp(@RequestBody LoginRequest request) {
        return authService.sendLoginOtp(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, @RequestParam String otp) {
        return authService.login(
                request, otp
        );
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String phone = request.get("phone");
            String otp = request.get("otp");

            if (otp == null || otp.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "OTP is required"));
            }

            // Use email or phone as identifier
            String identifier = email != null && !email.trim().isEmpty() ? email : phone;
            if (identifier == null || identifier.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email or phone is required"));
            }

            // Verify OTP but don't remove it yet (will be removed during registration)
            // This allows the OTP to be used for registration after verification
            boolean isValid = otpService.verifyOTP(identifier, otp, false);

            if (isValid) {
                return ResponseEntity.ok(Map.of(
                        "message", "OTP verified successfully",
                        "success", true
                ));
            } else {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "Invalid or expired OTP",
                        "success", false
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error: " + e.getMessage()));
        }
    }

}