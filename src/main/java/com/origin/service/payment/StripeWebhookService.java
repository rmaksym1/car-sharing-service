package com.origin.service.payment;

import com.origin.exception.EntityNotFoundException;
import com.origin.exception.payment.PaymentProcessingException;
import com.origin.model.Payment;
import com.origin.model.enums.PaymentStatus;
import com.origin.repository.payment.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripeWebhookService {
    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final PaymentRepository paymentRepository;

    public void handleWebhook(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow(
                                () -> new PaymentProcessingException("Invalid session payload")
                        );

                Payment payment = paymentRepository.findBySessionId(session.getId()).orElseThrow(
                        () -> new EntityNotFoundException("Cannot find payment by session id:"
                                + session.getId())
                );

                if (payment.getStatus() == PaymentStatus.PAID) {
                    return;
                }

                payment.setStatus(PaymentStatus.PAID);
                paymentRepository.save(payment);
            }

        } catch (SignatureVerificationException e) {
            throw new PaymentProcessingException("Invalid Stripe signature");
        } catch (Exception e) {
            throw new PaymentProcessingException("Webhook processing failed: " + e.getMessage());
        }
    }
}
