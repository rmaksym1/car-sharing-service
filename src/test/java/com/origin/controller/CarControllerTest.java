package com.origin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.origin.dto.car.CreateCarRequest;
import com.origin.dto.car.UpdateCarInventoryRequest;
import com.origin.model.enums.CarType;
import com.origin.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;

import static com.origin.util.TestConstants.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CarControllerTest {
    private static final String CARS_PATH = "/cars";
    private static final String CARS_ID_PATH = "/cars/{id}";
    private static final String CARS_ID_INVENTORY_PATH = "/cars/{id}/inventory";
    private static final Long ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @DisplayName("Should create a car successfully by role")
    @CsvSource({
            "MANAGER, 201",
            "CUSTOMER, 403"
    })
    @Sql(scripts = CLEANUP_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void createCar_ByRole_ReturnsResponse(String role, int expectedStatus) throws Exception {
        CreateCarRequest carRequest = TestUtil.createCarRequest();

        mockMvc.perform(post(CARS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user("test").roles(role))
                        .content(objectMapper.writeValueAsString(carRequest)))
                .andExpect(status().is(expectedStatus));
    }

    @ParameterizedTest
    @DisplayName("Should get cars successfully by role")
    @CsvSource({
            "Anonymous",
            "CUSTOMER"
    })
    void getAllCars_ByRole_ReturnsResponse(String role) throws Exception {
        var requestBuilder = get(CARS_PATH);

        if (role.equals("CUSTOMER")) {
            requestBuilder.with(user("test_user").roles(role));
        }

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @DisplayName("Should get a car successfully by role")
    @CsvSource({
            "Anonymous",
            "CUSTOMER"
    })
    @Sql(scripts = CLEANUP_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ADD_CAR_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getCarById_ByRole_ReturnsResponse(String role) throws Exception {
        var requestBuilder = get(CARS_PATH);

        if (role.equals("CUSTOMER")) {
            requestBuilder.with(user("test_user").roles(role));
        }

        mockMvc.perform(get(CARS_ID_PATH, ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return not found when car by invalid id is not found")
    @WithMockUser(roles = "CUSTOMER")
    @Sql(scripts = CLEANUP_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ADD_CAR_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getCarById_InvalidId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get(CARS_ID_PATH, INVALID_ID))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @DisplayName("Should update a car successfully by role")
    @CsvSource({
            "MANAGER, 200",
            "CUSTOMER, 403"
    })
    @Sql(scripts = CLEANUP_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ADD_CAR_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void updateCar_ByRole_ReturnsResponse(String role, int expectedStatus) throws Exception {
        CreateCarRequest carRequest = new CreateCarRequest("Civic", "Honda", CarType.HATCHBACK, 4, BigDecimal.valueOf(299.99));

        var result = mockMvc.perform(patch(CARS_ID_PATH, ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user("test").roles(role))
                        .content(objectMapper.writeValueAsString(carRequest)))
                .andExpect(status().is(expectedStatus));

        if (expectedStatus == 200) {
            result.andExpect(jsonPath("$.brand").value("Honda"))
                    .andExpect(jsonPath("$.model").value("Civic"));
        }
    }

    @Test
    @DisplayName("Should return not found when trying to update car by wrong id")
    @WithMockUser(roles = "MANAGER")
    @Sql(scripts = CLEANUP_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ADD_CAR_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void updateCar_InvalidId_ReturnsNotFound() throws Exception {
        CreateCarRequest carRequest = new CreateCarRequest("Civic", "Honda", CarType.HATCHBACK, 5, BigDecimal.valueOf(299.99));

        mockMvc.perform(patch(CARS_ID_PATH, INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carRequest)))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @DisplayName("Should update car inventory successfully by role")
    @CsvSource({
            "MANAGER, 200",
            "CUSTOMER, 403"
    })
    @Sql(scripts = CLEANUP_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ADD_CAR_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void updateCarInventory_ByRole_ReturnsResponse(String role, int expectedStatus) throws Exception {
        UpdateCarInventoryRequest request = TestUtil.createUpdateCarInventoryRequest();

        var result = mockMvc.perform(patch(CARS_ID_INVENTORY_PATH, ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user("test").roles(role))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus));

        if (expectedStatus == 200) {
            result.andExpect(jsonPath("$.inventory").value(10));
        }
    }

    @Test
    @DisplayName("Should return not found when trying to update car inventory by wrong id")
    @WithMockUser(roles = "MANAGER")
    @Sql(scripts = CLEANUP_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ADD_CAR_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void updateCarInventory_InvalidId_ReturnsNotFound() throws Exception {
        UpdateCarInventoryRequest request = TestUtil.createUpdateCarInventoryRequest();

        mockMvc.perform(patch(CARS_ID_INVENTORY_PATH, INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @DisplayName("Should delete car successfully by role")
    @CsvSource({
            "MANAGER, 204",
            "CUSTOMER, 403"
    })
    @Sql(scripts = CLEANUP_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = ADD_CAR_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void deleteCar_ByRole_ReturnsResponse(String role, int expectedStatus) throws Exception {
        mockMvc.perform(delete(CARS_ID_PATH, ID)
                        .with(user("test").roles(role)))
                .andExpect(status().is(expectedStatus));

        if (expectedStatus == 204) {
            mockMvc.perform(get(CARS_PATH)
                    .with(user("test").roles(role)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }
}
