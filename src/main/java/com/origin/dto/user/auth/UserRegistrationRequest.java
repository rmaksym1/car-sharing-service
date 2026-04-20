package com.origin.dto.user.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
        @NotBlank(message = "Email cannot be blank")
        @Size(min = 8, max = 50)
        String email,
        @NotBlank(message = "First name cannot be blank")
        @Size(min = 1, max = 20)
        String firstName,
        @NotBlank(message = "Last name cannot be blank")
        @Size(min = 1, max = 20)
        String lastName,
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, max = 20)
        String password
) {}
