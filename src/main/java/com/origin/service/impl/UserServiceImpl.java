package com.origin.service.impl;

import com.origin.dto.user.UserResponse;
import com.origin.dto.user.profile.UpdateUserRoleRequest;
import com.origin.dto.user.profile.UserInfoUpdateRequest;
import com.origin.exception.EntityNotFoundException;
import com.origin.mapper.UserMapper;
import com.origin.model.User;
import com.origin.repository.user.UserRepository;
import com.origin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public void updateUserRole(Long id, UpdateUserRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User by id: " + id + " not found"));

        user.setRole(request.role());
        userRepository.save(user);
    }

    @Override
    public UserResponse getUserInfo(User user) {
        return userMapper.toDto(user);
    }

    @Override
    public UserResponse updateUserInfo(User user, UserInfoUpdateRequest request) {
        userMapper.updateUser(request, user);

        return userMapper.toDto(userRepository.save(user));
    }
}
