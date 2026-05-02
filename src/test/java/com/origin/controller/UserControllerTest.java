package com.origin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.origin.dto.user.profile.UpdateUserRoleRequest;
import com.origin.dto.user.profile.UserInfoUpdateRequest;
import com.origin.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static com.origin.util.TestConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    private static final String UPDATE_ROLE_URL = "/users/{id}/role";
    private static final String ME_URL = "/users/me";
    private static final Long USER_ID = 5L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should successfully update user role by admin")
    @WithMockUser(roles = ADMIN_ROLE)
    @Sql(scripts = CLEANUP_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ADD_USER_DB_PATH,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void updateUserRole_AsAdmin_ReturnsNoContent() throws Exception {
        UpdateUserRoleRequest request = TestUtil.createUpdateUserRoleRequest();

        mockMvc.perform(put(UPDATE_ROLE_URL, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should successfully update user role by admin")
    @WithMockUser(roles = CUSTOMER_ROLE)
    @Sql(scripts = CLEANUP_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ADD_USER_DB_PATH,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void updateUserRole_AsCustomer_ReturnsForbidden() throws Exception {
        UpdateUserRoleRequest request = TestUtil.createUpdateUserRoleRequest();

        mockMvc.perform(put(UPDATE_ROLE_URL, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should successfully get current user info")
    @WithUserDetails("user@gmail.com")
    @Sql(scripts = CLEANUP_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ADD_USER_DB_PATH,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getCurrentUserInfo_ReturnsUserResponse() throws Exception {
        mockMvc.perform(get(ME_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.email").value("user@gmail.com")
                );
    }

    @Test
    @DisplayName("Should successfully update current user info")
    @WithUserDetails("user@gmail.com")
    @Sql(scripts = CLEANUP_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ADD_USER_DB_PATH,
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void updateCurrentUserInfo_ReturnsUserResponse() throws Exception {
        UserInfoUpdateRequest request = TestUtil.createUserInfoUpdateRequest();

        mockMvc.perform(patch(ME_URL, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
