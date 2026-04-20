package com.origin.dto.user.profile;

import com.origin.model.User;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull(message = "Role cannot be null!")
        User.Role role
) {}
