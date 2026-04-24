package com.origin.service.payment;

import com.origin.model.Rental;
import com.origin.model.enums.PaymentType;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {
    private static final String baseUrl = "http://localhost:8088";

    @Value("${stripe.secret.key}")
    private String secretKey;

    public Session createStripeSession(
            Rental rental,
            BigDecimal amount,
            PaymentType paymentType) throws StripeException {
        long centsAmount = amount.multiply(BigDecimal.valueOf(100)).longValueExact();

        String title = paymentType == PaymentType.PAYMENT
                ? "RENTAL: " + rental.getCar().getBrand() + " " + rental.getCar().getModel()
                : "FINE: " + rental.getCar().getBrand() + " " + rental.getCar().getModel();

        String description = paymentType == PaymentType.PAYMENT
                ? "Period: " + rental.getRentalDate() + " to " + rental.getReturnDate()
                : "OVERDUE: "
                + Math.max(0, ChronoUnit.DAYS.between(rental.getReturnDate(),
                        rental.getActualReturnDate()))
                + " days (" + rental.getReturnDate() + " to "
                + rental.getActualReturnDate() + ")";

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(baseUrl + "/api/payments/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(baseUrl + "/api/payments/cancel?session_id={CHECKOUT_SESSION_ID}")
                .setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES).getEpochSecond())
                .putMetadata("rental_id", rental.getId().toString())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(centsAmount)
                                .setProductData(SessionCreateParams.LineItem.PriceData
                                        .ProductData.builder()
                                        .setName(title)
                                        .setDescription(description)
                                        .build())
                                .build())
                        .build())
                .build();
        return Session.create(params);
    }

    public Session getStripeSession(String sessionId) throws StripeException {
        return Session.retrieve(sessionId);
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
}
