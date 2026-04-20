package com.origin.mapper;

import com.origin.config.MapperConfig;
import com.origin.dto.user.UserResponse;
import com.origin.dto.user.auth.UserRegistrationRequest;
import com.origin.dto.user.profile.UserInfoUpdateRequest;
import com.origin.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponse toDto(User user);

    User toModel(UserRegistrationRequest userRegistrationRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(UserInfoUpdateRequest request, @MappingTarget User user);
}
