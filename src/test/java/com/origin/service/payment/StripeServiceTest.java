package com.origin.service.payment;

import com.origin.model.Car;
import com.origin.model.Rental;
import com.origin.model.enums.PaymentType;
import com.origin.util.TestUtil;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StripeServiceTest {

    @InjectMocks
    private StripeService stripeService;

    @Test
    @DisplayName("Should create payment stripe session with correct parameters")
    void createStripeSessionPayment_ValidRequest_ReturnsSession() throws StripeException {
        Car car = TestUtil.createCar();

        Rental rental = TestUtil.createRental(TestUtil.createUser(), car);
        rental.setId(1L);

        BigDecimal amount = new BigDecimal("100.00");

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            Session mockSession = mock(Session.class);
            when(mockSession.getId()).thenReturn("test_id");
            when(mockSession.getUrl()).thenReturn("https://test.url");

            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            Session result = stripeService.createStripeSession(rental, amount, PaymentType.PAYMENT);

            assertNotNull(result);
            assertEquals("test_id", result.getId());
            assertEquals("https://test.url", result.getUrl());

            mockedSession.verify(() -> Session.create(any(SessionCreateParams.class)));
        }
    }

    @Test
    @DisplayName("Should create fine stripe session with correct parameters")
    void createStripeSessionFine_ValidRequest_ReturnsSession() throws StripeException {
        Car car = TestUtil.createCar();

        Rental rental = TestUtil.createRental(TestUtil.createUser(), car);
        rental.setId(1L);
        rental.setReturnDate(LocalDate.now());
        rental.setActualReturnDate(LocalDate.now().plusDays(5));

        BigDecimal amount = new BigDecimal("100.00");

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            Session mockSession = mock(Session.class);
            when(mockSession.getId()).thenReturn("test_id");
            when(mockSession.getUrl()).thenReturn("https://test.url");

            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            Session result = stripeService.createStripeSession(rental, amount, PaymentType.FINE);

            assertNotNull(result);
            assertEquals("test_id", result.getId());
            assertEquals("https://test.url", result.getUrl());

            mockedSession.verify(() -> Session.create(any(SessionCreateParams.class)));
        }
    }

    @Test
    @DisplayName("Should return an ongoing session with correct session id")
    void getSession_ValidRequest_ReturnsSession() throws StripeException {
        String sessionId = "test_session_id";

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            Session mockSession = mock(Session.class);

            mockedSession.when(() -> Session.retrieve(sessionId))
                    .thenReturn(mockSession);

            Session actual = stripeService.getStripeSession(sessionId);

            assertNotNull(actual);
            assertEquals(mockSession, actual);

            mockedSession.verify(() -> Session.retrieve(sessionId));
        }
    }

    @Test
    @DisplayName("Should throw exception when session is not found")
    void getStripeSession_InvalidId_ThrowsException() {
        String invalidSessionId = "invalid_id";

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(invalidSessionId))
                    .thenThrow(new InvalidRequestException(
                            "No such checkout.session: " + invalidSessionId, null,
                            "session", "code", 404, null));

            assertThrows(StripeException.class,
                    () -> stripeService.getStripeSession(invalidSessionId)
            );

            mockedSession.verify(() -> Session.retrieve(invalidSessionId));
        }
    }
}
