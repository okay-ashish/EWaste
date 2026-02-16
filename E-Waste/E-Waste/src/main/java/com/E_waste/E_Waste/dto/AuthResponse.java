package com.E_waste.E_Waste.dto;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;


}
