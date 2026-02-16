package com.E_waste.E_Waste.Controller;

import com.E_waste.E_Waste.Service.JwtService;
import com.E_waste.E_Waste.dto.AuthRequest;
import com.E_waste.E_Waste.dto.UserProfileResponse;
import com.E_waste.E_Waste.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping("/profile")
    public UserProfileResponse getProfile(@RequestHeader("Authorization") String token) {
        String email = jwtService.extractEmail(token.substring(7));
        return userService.getProfile(email);
    }

    @PatchMapping("/update")
    public UserProfileResponse updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody AuthRequest request) {

        String email = jwtService.extractEmail(token.substring(7));
        return userService.updateProfile(email, request);
    }
}
