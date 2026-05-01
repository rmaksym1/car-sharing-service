package com.origin.service;

import com.origin.dto.user.UserResponse;
import com.origin.dto.user.auth.UserLoginRequest;
import com.origin.dto.user.auth.UserLoginResponse;
import com.origin.dto.user.auth.UserRegistrationRequest;
import com.origin.exception.RegistrationException;
import com.origin.mapper.UserMapper;
import com.origin.model.User;
import com.origin.repository.user.UserRepository;
import com.origin.security.JwtUtil;
import com.origin.service.impl.AuthenticationServiceImpl;
import com.origin.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    @DisplayName("Should successfully save user")
    void saveUser_ReturnsUserResponse() {
        User user = TestUtil.createUser();
        UserResponse userResponse = TestUtil.createUserResponse();
        UserRegistrationRequest registrationRequest = TestUtil.createUserRegistrationRequest();

        when(userRepository.existsByEmail(registrationRequest.email())).thenReturn(false);
        when(userMapper.toModel(registrationRequest)).thenReturn(user);
        when(passwordEncoder.encode(registrationRequest.password())).thenReturn(user.getPassword());
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponse);

        UserResponse actual = authenticationService.save(registrationRequest);

        assertEquals(userResponse, actual);
        verify(userMapper).toModel(registrationRequest);
        verify(passwordEncoder).encode(registrationRequest.password());
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("Should throw exception when saving user with existing email")
    void saveUserWithExistingEmail_ThrowsException() {
        UserRegistrationRequest registrationRequest = TestUtil.createUserRegistrationRequest();

        when(userRepository.existsByEmail(registrationRequest.email())).thenReturn(true);

        assertThrows(RegistrationException.class,
                () -> authenticationService.save(registrationRequest)
        );
        verifyNoInteractions(userMapper);
        verifyNoInteractions(passwordEncoder);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Should authenticate user and return token")
    void authenticate_ReturnsToken() {
        UserLoginRequest request = TestUtil.createUserLoginRequest();

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@gmail.com");
        when(jwtUtil.generateToken("user@gmail.com")).thenReturn("jwt-token");

        UserLoginResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        verify(authenticationManager).authenticate(any());
        verify(jwtUtil).generateToken("user@gmail.com");
    }
}
