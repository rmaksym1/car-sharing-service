package com.origin.dto.user;

import com.origin.model.User;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        User.Role role
) {}
