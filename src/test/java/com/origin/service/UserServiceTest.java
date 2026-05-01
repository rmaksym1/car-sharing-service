package com.origin.service;

import com.origin.dto.user.UserResponse;
import com.origin.dto.user.profile.UpdateUserRoleRequest;
import com.origin.dto.user.profile.UserInfoUpdateRequest;
import com.origin.exception.EntityNotFoundException;
import com.origin.mapper.UserMapper;
import com.origin.model.User;
import com.origin.repository.user.UserRepository;
import com.origin.service.impl.UserServiceImpl;
import com.origin.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private static final Long ID = 1L;
    private static final Long INCORRECT_ID = 999L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should successfully update user role")
    void updateUserRole_success() {
        User user = TestUtil.createUser();
        UpdateUserRoleRequest request = TestUtil.createUpdateUserRoleRequest();
        User updatedUser = TestUtil.createUser();
        updatedUser.setRole(User.Role.MANAGER);

        when(userRepository.findById(ID)).thenReturn(Optional.of(user));

        userService.updateUserRole(ID, request);

        assertEquals(User.Role.MANAGER, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw an exception if user not found while updating role")
    void updateIncorrectUserRole_throwsException() {
        UpdateUserRoleRequest request = TestUtil.createUpdateUserRoleRequest();

        when(userRepository.findById(INCORRECT_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.updateUserRole(INCORRECT_ID, request)
        );
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should successfully get user info")
    void getUserInfo_ReturnsUserInfo() {
        User user = TestUtil.createUser();
        UserResponse userResponse = TestUtil.createUserResponse();

        when(userMapper.toDto(user)).thenReturn(userResponse);

        UserResponse actual = userService.getUserInfo(user);

        assertEquals(userResponse.email(), actual.email());
        verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("Should successfully update user info")
    void updateUserInfo_updatesUserInfo() {
        UserInfoUpdateRequest request = TestUtil.createUserInfoUpdateRequest();
        User user = TestUtil.createUser();
        UserResponse userResponse = TestUtil.createUserResponse();

        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponse);

        UserResponse actual = userService.updateUserInfo(user, request);

        assertEquals(userResponse, actual);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }
}
