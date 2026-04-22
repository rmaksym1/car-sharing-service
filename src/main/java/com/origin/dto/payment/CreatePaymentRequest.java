package com.origin.dto.payment;

import com.origin.model.enums.PaymentType;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(
        @NotNull(message = "Rental ID cannot be null!")
        Long rentalId,

        @NotNull(message = "Payment type cannot be null!")
        PaymentType paymentType
) {}
