package com.E_waste.E_Waste.dto;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
public class UserProfileResponse {

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String pincode;
    private Boolean isVerified;
}

