package com.origin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.origin.dto.payment.CreatePaymentRequest;
import com.origin.dto.rental.CreateRentalRequest;
import com.origin.model.Rental;
import com.origin.model.enums.PaymentType;
import com.origin.service.payment.StripeService;
import com.origin.util.TestUtil;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import javax.swing.text.StringContent;

import java.math.BigDecimal;

import static com.origin.util.TestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTest {
    private static final String PAYMENTS_PATH = "/payments";
    private static final String PAYMENTS_SUCCESS_PATH = "/payments/success";
    private static final String PAYMENTS_CANCEL_PATH = "/payments/cancel";
    private static final String PAYMENTS_ID_PATH = "/payments/{rentalId}";
    private static final Long ID = 2L;
    private static final Long INVALID_ID = 999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StripeService stripeService;

    @Test
    @DisplayName("Should create a payment successfully by customer")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_RENTAL_DB_PATH},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails("user@gmail.com")
    void createPaymentCustomer_ValidRequest_ReturnsResponse() throws Exception {
        CreatePaymentRequest createPaymentRequest = TestUtil.createPaymentRequest(PaymentType.PAYMENT);
        Session session = mock(Session.class);

        when(session.getId()).thenReturn("test_id");
        when(session.getUrl()).thenReturn("http://testurl.com");
        when(stripeService.createStripeSession(any(Rental.class), any(BigDecimal.class), eq(createPaymentRequest.paymentType())))
                .thenReturn(session);

        mockMvc.perform(post(PAYMENTS_ID_PATH, ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionUrl").value("http://testurl.com"));
    }

    @Test
    @DisplayName("Should return forbidden when creating a payment by anonymous")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_RENTAL_DB_PATH},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void createPaymentAnonymous_ValidRequest_ReturnsForbidden() throws Exception {
        CreatePaymentRequest createPaymentRequest = TestUtil.createPaymentRequest(PaymentType.PAYMENT);

        mockMvc.perform(post(PAYMENTS_ID_PATH, ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return payments by user id for user")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_PAYMENT_DB_PATH},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails("user@gmail.com")
    void getPaymentsByUserId_ValidRequest_ReturnsResponse() throws Exception {
        mockMvc.perform(get(PAYMENTS_PATH)
                        .param("userId", String.valueOf(ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should return payments by user id for manager")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_MANAGER_DB_PATH, ADD_PAYMENT_DB_PATH},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails("manager@gmail.com")
    void getPaymentsByUserIdManager_ValidRequest_ReturnsResponse() throws Exception {
        mockMvc.perform(get(PAYMENTS_PATH)
                        .param("userId", String.valueOf(ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should return empty list of payments by user id for manager")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_MANAGER_DB_PATH, ADD_PAYMENT_DB_PATH},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails("manager@gmail.com")
    void getPaymentsByInvalidUserIdManager_ValidRequest_ReturnsResponse() throws Exception {
        mockMvc.perform(get(PAYMENTS_PATH)
                        .param("userId", String.valueOf(INVALID_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("Should return string when cancelling a payment")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_MANAGER_DB_PATH, ADD_PAYMENT_DB_PATH},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails("user@gmail.com")
    void processCancel_ValidRequest_ReturnsResponse() throws Exception {
        mockMvc.perform(get(PAYMENTS_CANCEL_PATH)
                        .param("session_id", "test_id"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment canceled. You can return to website or try again"));
    }

    @Test
    @DisplayName("Should return not found when trying to cancel with invalid session id")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_MANAGER_DB_PATH, ADD_PAYMENT_DB_PATH},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails("user@gmail.com")
    void processCancelByInvalidSessionId_BadRequest_ReturnsNotFound() throws Exception {
        mockMvc.perform(get(PAYMENTS_CANCEL_PATH)
                        .param("session_id", "invalid_id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return string when completing a payment")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_MANAGER_DB_PATH, ADD_PAYMENT_DB_PATH},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails("user@gmail.com")
    void processSuccess_ValidRequest_ReturnsResponse() throws Exception {
        Session session = mock(Session.class);

        when(session.getId()).thenReturn("test_id");
        when(session.getUrl()).thenReturn("http://testurl.com");
        when(session.getPaymentStatus()).thenReturn("paid");
        when(stripeService.getStripeSession("test_id"))
                .thenReturn(session);

        mockMvc.perform(get(PAYMENTS_SUCCESS_PATH)
                        .param("session_id", "test_id"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment successful! Return to website"));
    }

    @Test
    @DisplayName("Should return not found when trying to cancel with invalid session id")
    @Sql(scripts = {CLEANUP_DB_PATH, ADD_MANAGER_DB_PATH, ADD_PAYMENT_DB_PATH},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @WithUserDetails("user@gmail.com")
    void processSuccessByInvalidSessionId_BadRequest_ReturnsNotFound() throws Exception {
        mockMvc.perform(get(PAYMENTS_SUCCESS_PATH)
                        .param("session_id", "invalid_id"))
                .andExpect(status().isNotFound());
    }
}
