package com.origin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.origin.dto.rental.CreateRentalRequest;
import com.origin.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;

import static com.origin.util.TestConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RentalControllerTest {
    private static final String RENTALS_PATH = "/rentals";
    private static final String RENTALS_ID_PATH = "/rentals/{id}";
    private static final Long ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create a rental successfully by customer")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_CAR_DB_PATH, ADD_USER_DB_PATH},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails("user@gmail.com")
    void createRental_ValidRequest_ReturnsResponse() throws Exception {
        CreateRentalRequest createRentalRequest = TestUtil.createRentalRequest();

        mockMvc.perform(post(RENTALS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRentalRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return forbidden when creating a rental by anonymous")
    @Sql(scripts = CLEANUP_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void createRental_Anonymous_ReturnsForbidden() throws Exception {
        CreateRentalRequest createRentalRequest = TestUtil.createRentalRequest();

        mockMvc.perform(post(RENTALS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRentalRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return response when getting all rentals by customer")
    @WithUserDetails("user@gmail.com")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_RENTAL_DB_PATH}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getAllRentalsByUser_ValidRequest_ReturnsResponse() throws Exception {
        mockMvc.perform(get(RENTALS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("Should return response when getting all rentals by manager")
    @WithUserDetails("manager@gmail.com")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_MANAGER_DB_PATH, ADD_RENTAL_DB_PATH}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getAllRentalsByManager_ValidRequest_ReturnsResponse() throws Exception {
        mockMvc.perform(get(RENTALS_PATH)
                        .param("userId", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("Should return forbidden when getting all rentals by anonymous")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_RENTAL_DB_PATH}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getAllRentalsByAnonymous_ValidRequest_ReturnsForbidden() throws Exception {
        mockMvc.perform(get(RENTALS_PATH))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return response when getting a rental by customer")
    @WithUserDetails("user@gmail.com")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_RENTAL_DB_PATH}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getRentalByIdCustomer_ValidRequest_ReturnsResponse() throws Exception {
        mockMvc.perform(get(RENTALS_ID_PATH, ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.car.model").value("Audi"));
    }

    @Test
    @DisplayName("Should return response when getting a rental by manager")
    @WithUserDetails("manager@gmail.com")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_MANAGER_DB_PATH, ADD_RENTAL_DB_PATH}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getRentalByIdManager_ValidRequest_ReturnsResponse() throws Exception {
        mockMvc.perform(get(RENTALS_ID_PATH, ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.car.model").value("Audi"));
    }

    @Test
    @DisplayName("Should return forbidden when getting rental by id by anonymous")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_RENTAL_DB_PATH}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getRentalByAnonymous_ValidRequest_ReturnsForbidden() throws Exception {
        mockMvc.perform(get(RENTALS_ID_PATH, ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return not found when getting a rental with invalid id by customer")
    @WithUserDetails("user@gmail.com")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_RENTAL_DB_PATH}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getRentalByInvalidIdCustomer_BadRequest_ReturnsResponse() throws Exception {
        mockMvc.perform(get(RENTALS_ID_PATH, INVALID_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return response when setting actual return date to rental by customer")
    @WithUserDetails("user@gmail.com")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_RENTAL_DB_PATH}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void setActualReturnDateCustomer_ValidRequest_ReturnsResponse() throws Exception {
        mockMvc.perform(post(RENTALS_ID_PATH + "/return", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.car.model").value("Audi"))
                .andExpect(jsonPath("$.actualReturnDate").value(LocalDate.now().toString()));
    }

    @Test
    @DisplayName("Should return not found when setting actual return date to invalid rental by customer")
    @WithUserDetails("user@gmail.com")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_RENTAL_DB_PATH}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void setActualReturnDateCustomer_BadRequest_ReturnsNotFound() throws Exception {
        mockMvc.perform(post(RENTALS_ID_PATH + "/return", INVALID_ID))
                .andExpect(status().isNotFound());
    }
}
