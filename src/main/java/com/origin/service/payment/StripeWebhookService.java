package com.origin.service.payment;

import com.origin.exception.EntityNotFoundException;
import com.origin.exception.payment.PaymentProcessingException;
import com.origin.model.Payment;
import com.origin.model.enums.PaymentStatus;
import com.origin.notification.NotificationService;
import com.origin.repository.payment.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripeWebhookService {
    private static final Logger log = LogManager.getLogger(StripeWebhookService.class);
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    public void handleWebhook(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            switch (event.getType()) {
                case "checkout.session.completed":
                    updatePaymentStatus(getSession(event).getId(), PaymentStatus.PAID);
                    break;
                case "checkout.session.expired":
                    updatePaymentStatus(getSession(event).getId(), PaymentStatus.EXPIRED);
                    break;
                default:
                    log.warn("Unhandled Stripe webhook event: {}", event.getType());
            }

        } catch (SignatureVerificationException e) {
            log.error("Invalid stripe signature", e);
        }
    }

    private void updatePaymentStatus(String sessionId, PaymentStatus status) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Cannot find payment by session id:"
                        + sessionId)
        );

        if (payment.getStatus() == status) {
            return;
        }

        payment.setStatus(status);
        paymentRepository.save(payment);
        sendNotification(payment, status);
    }

    private void sendNotification(Payment payment, PaymentStatus status) {
        switch (status) {
            case PAID -> notificationService.sendPaymentSucceededMessage(payment);
            default -> { }
        }
    }

    private Session getSession(Event event) {
        return (Session) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(
                        () -> new PaymentProcessingException("Invalid session payload")
                );
    }
}
