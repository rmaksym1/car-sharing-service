package com.origin.dto.payment;

import com.origin.model.enums.PaymentStatus;
import com.origin.model.enums.PaymentType;
import java.math.BigDecimal;
import java.net.URL;

public record PaymentResponse(
        Long id,
        PaymentStatus status,
        PaymentType type,
        Long rentalId,
        URL sessionUrl,
        String sessionId,
        BigDecimal amountToPay
) {}
