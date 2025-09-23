package com.example.demo.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneVerificationRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^60\\d{9,10}$",
        message = "Phone number must start with 60 and contain 11-12 digits total"
    )
    private String phoneNumber;

    @NotBlank(message = "Verification code is required")
    @Size(min = 6, max = 6, message = "Verification code must be 6 characters")
    private String verificationCode;
}
