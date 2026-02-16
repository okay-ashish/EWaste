package com.E_waste.E_Waste.Service;

import com.E_waste.E_Waste.dto.*;
import com.E_waste.E_Waste.Entity.*;
import com.E_waste.E_Waste.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OTPService otpService;
    private final EmailService emailService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public ResponseEntity<?> sendRegisterOtp(Map<String,String> request) {

        try {
            String email = request.get("email");
            String phone = request.get("phone");
            Boolean useEmail = request.get("useEmail") != null ?
                    Boolean.parseBoolean(request.get("useEmail")) : true;

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email already registered"));
            }

            // Generate OTP
            System.out.println("DEBUG: Generating OTP for email: " + email);
            String otp = otpService.generateOTP(email);
            System.out.println("DEBUG: OTP generated: " + otp);

            // Send OTP via email
            if (useEmail) {
                System.out.println("DEBUG: Sending OTP via email service...");
                boolean sent = emailService.sendOTPEmail(email, otp);
                System.out.println("DEBUG: Email service returned: " + sent);
                if (sent) {
                    return ResponseEntity.ok(Map.of(
                            "message", "OTP sent successfully to " + email,
                            "success", true
                    ));
                } else {
                    return ResponseEntity.status(500).body(Map.of("error", "Failed to send OTP"));
                }
            } else {
                // SMS sending can be added here later
                return ResponseEntity.badRequest().body(Map.of("error", "SMS not implemented yet"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error: " + e.getMessage()));
        }
    }


    public ResponseEntity<?> register(AuthRequest request, String enteredOtp) {

        boolean otpValid = otpService.verifyOTP(request.getEmail(), enteredOtp, true);

        if (!otpValid) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid or expired OTP"));
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .pincode(request.getPincode())
                .isVerified(true)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    public ResponseEntity<?> sendLoginOtp(LoginRequest request) {

        String email= request.getEmail();
        String password= request.getPassword();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "User not registered"));
        }

        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());

        if (!passwordMatches) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid email or password"));
        }

        String otp = otpService.generateOTP(email);

        emailService.sendOTPEmail(email, otp);

        return ResponseEntity.ok(
                Map.of("message", "OTP sent for login")
        );
    }



    public ResponseEntity<?> login(LoginRequest request,String otp) {
        try {
            boolean otpValid = otpService.verifyOTP(request.getEmail(), otp, true);

            if (!otpValid) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid or expired OTP"));
            }

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
            }

            if (user.getIsVerified() == null || !user.getIsVerified()) {
                user.setIsVerified(true);
                userService.updateProfile(user.getEmail(), user);
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            String token2 = jwtService.generateToken(user);

            return ResponseEntity.ok(new AuthResponse(token2, user.getEmail()));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password", "message", "Please check your email and password"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }
}

