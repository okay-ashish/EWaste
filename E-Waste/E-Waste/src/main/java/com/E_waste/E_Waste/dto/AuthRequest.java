package com.E_waste.E_Waste.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class AuthRequest {

    @NotBlank
    private String firstName;

    private String lastName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String phoneNumber;
    private String address;
    private String pincode;
//    private Boolean isVerified = false;

}
