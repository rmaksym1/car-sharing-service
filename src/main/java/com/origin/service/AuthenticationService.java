package com.origin.service;

import com.origin.dto.user.UserResponse;
import com.origin.dto.user.auth.UserLoginRequest;
import com.origin.dto.user.auth.UserLoginResponse;
import com.origin.dto.user.auth.UserRegistrationRequest;

public interface AuthenticationService {
    UserResponse save(UserRegistrationRequest request);

    UserLoginResponse authenticate(UserLoginRequest request);
}
