package com.origin.controller;

import com.origin.dto.user.UserResponse;
import com.origin.dto.user.auth.UserLoginRequest;
import com.origin.dto.user.auth.UserLoginResponse;
import com.origin.dto.user.auth.UserRegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth management", description = "Endpoints for managing authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
    private final com.origin.service.AuthenticationService authenticationService;

    @Operation(summary = "Create new user", description = "Create a new user")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/registration")
    public UserResponse register(@RequestBody @Valid UserRegistrationRequest request) {
        return authenticationService.save(request);
    }

    @Operation(summary = "User login", description = "Endpoint for user login")
    @PostMapping("/login")
    public UserLoginResponse login(@RequestBody @Valid UserLoginRequest request) {
        return authenticationService.authenticate(request);
    }
}
