package com.origin.service;

import com.origin.dto.payment.CreatePaymentRequest;
import com.origin.dto.payment.PaymentResponse;
import com.origin.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request);

    Page<PaymentResponse> getPaymentsByUserId(Long userId, User user, Pageable pageable);

    void processSuccess(String sessionId);

    void processCancel(String sessionId);
}
