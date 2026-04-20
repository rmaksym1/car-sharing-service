package com.origin.service;

import com.origin.dto.user.UserResponse;
import com.origin.dto.user.profile.UpdateUserRoleRequest;
import com.origin.dto.user.profile.UserInfoUpdateRequest;
import com.origin.model.User;

public interface UserService {
    void updateUserRole(Long id, UpdateUserRoleRequest request);

    UserResponse getUserInfo(User user);

    UserResponse updateUserInfo(User user, UserInfoUpdateRequest request);
}
