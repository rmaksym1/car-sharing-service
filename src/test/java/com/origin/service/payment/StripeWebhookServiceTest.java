package com.origin.service.payment;

import com.origin.exception.EntityNotFoundException;
import com.origin.exception.payment.PaymentProcessingException;
import com.origin.model.Payment;
import com.origin.model.Rental;
import com.origin.model.enums.PaymentStatus;
import com.origin.notification.NotificationService;
import com.origin.repository.payment.PaymentRepository;
import com.origin.util.TestUtil;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StripeWebhookServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private StripeWebhookService stripeWebhookService;

    @Test
    @DisplayName("Should update status to PAID on checkout.session.completed")
    void handleWebhook_SessionCompleted_UpdatesStatus() {
        String payload = "{}";
        String sigHeader = "valid_sig";
        String sessionId = "test_session_id";

        Event event = mock(Event.class);
        Session session = mock(Session.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);

        when(event.getType()).thenReturn("checkout.session.completed");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(session));
        when(session.getId()).thenReturn(sessionId);

        Payment payment = new Payment();
        payment.setSessionId(sessionId);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(anyString(), anyString(), any()))
                    .thenReturn(event);

            stripeWebhookService.handleWebhook(payload, sigHeader);

            assertEquals(PaymentStatus.PAID, payment.getStatus());
            verify(paymentRepository).save(payment);
            verify(notificationService).sendPaymentSucceededMessage(payment);
        }
    }

    @Test
    @DisplayName("Should update status to EXPIRED on checkout.session.expired")
    void handleWebhook_SessionExpired_UpdatesStatus() {
        String payload = "{}";
        String sigHeader = "valid_sig";
        String sessionId = "test_session_id";

        Event event = mock(Event.class);
        Session session = mock(Session.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);

        when(event.getType()).thenReturn("checkout.session.expired");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(session));
        when(session.getId()).thenReturn(sessionId);

        Payment payment = TestUtil.createPayment(new Rental());
        payment.setSessionId(sessionId);
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(anyString(), anyString(), any()))
                    .thenReturn(event);

            stripeWebhookService.handleWebhook(payload, sigHeader);

            assertEquals(PaymentStatus.EXPIRED, payment.getStatus());
            verify(paymentRepository).save(payment);
        }
    }

    @Test
    @DisplayName("Should log error and do nothing when signature is invalid")
    void handleWebhook_InvalidSignature_DoesNothing() {
        String payload = "invalid_payload";
        String sigHeader = "wrong_sig";

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(anyString(), anyString(), any()))
                    .thenThrow(new SignatureVerificationException("Invalid signature", "header"));

            stripeWebhookService.handleWebhook(payload, sigHeader);

            verifyNoInteractions(paymentRepository);
            verifyNoInteractions(notificationService);
        }
    }

    @Test
    @DisplayName("Should return when payment's status is already expired")
    void handleWebhook_AlreadyExpiredStatus_DoNothing() {
        String payload = "{}";
        String sigHeader = "valid_sig";
        String sessionId = "test_session_id";

        Event event = mock(Event.class);
        Session session = mock(Session.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);

        when(event.getType()).thenReturn("checkout.session.expired");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(session));
        when(session.getId()).thenReturn(sessionId);

        Payment payment = TestUtil.createPayment(new Rental());
        payment.setSessionId(sessionId);
        payment.setStatus(PaymentStatus.EXPIRED);

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(anyString(), anyString(), any()))
                    .thenReturn(event);

            stripeWebhookService.handleWebhook(payload, sigHeader);

            assertEquals(PaymentStatus.EXPIRED, payment.getStatus());
            verifyNoMoreInteractions(paymentRepository);
            verifyNoInteractions(notificationService);
        }
    }

    @Test
    @DisplayName("Should throw an exception if payment by sessionId not found")
    void handleWebhook_NoPayment_ThrowsException() {
        String payload = "{}";
        String sigHeader = "valid_sig";
        String sessionId = "test_session_id";

        Event event = mock(Event.class);
        Session session = mock(Session.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);

        when(event.getType()).thenReturn("checkout.session.expired");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(session));
        when(session.getId()).thenReturn(sessionId);
        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(anyString(), anyString(), any()))
                    .thenReturn(event);

            assertThrows(EntityNotFoundException.class,
                    () -> stripeWebhookService.handleWebhook(payload, sigHeader)
            );
            verifyNoMoreInteractions(paymentRepository);
            verifyNoInteractions(notificationService);
        }
    }

    @Test
    @DisplayName("Should throw an exception if session is not valid")
    void handleWebhook_InvalidSession_ThrowsException() {
        String payload = "{}";
        String sigHeader = "valid_sig";

        Event event = mock(Event.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);

        when(event.getType()).thenReturn("checkout.session.expired");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.empty());

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(anyString(), anyString(), any()))
                    .thenReturn(event);

            assertThrows(PaymentProcessingException.class,
                    () -> stripeWebhookService.handleWebhook(payload, sigHeader)
            );
            verifyNoInteractions(notificationService, paymentRepository);
        }
    }

    @Test
    @DisplayName("Should log a warn and do nothing on unsupported event")
    void handleUnsupportedWebhook_DoesNothing() {
        String payload = "{}";
        String sigHeader = "valid_sig";

        Event event = mock(Event.class);
        when(event.getType()).thenReturn("checkout.other.event");

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(anyString(), anyString(), any()))
                    .thenReturn(event);

            stripeWebhookService.handleWebhook(payload, sigHeader);

            verifyNoInteractions(paymentRepository);
            verifyNoInteractions(notificationService);
        }
    }
}
