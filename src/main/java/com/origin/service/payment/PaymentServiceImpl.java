package com.origin.service.payment;

import com.origin.dto.payment.CreatePaymentRequest;
import com.origin.dto.payment.PaymentResponse;
import com.origin.exception.EntityNotFoundException;
import com.origin.exception.payment.PaymentProcessingException;
import com.origin.mapper.PaymentMapper;
import com.origin.model.Payment;
import com.origin.model.Rental;
import com.origin.model.User;
import com.origin.model.enums.PaymentStatus;
import com.origin.repository.payment.PaymentRepository;
import com.origin.repository.rental.RentalRepository;
import com.origin.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final StripeService stripeService;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Rental rental = rentalRepository.findById(request.rentalId()).orElseThrow(
                () -> new EntityNotFoundException("Rental by id: "
                        + request.rentalId()
                        + " not found!")
        );

        BigDecimal rentPrice = calculateTotalRentPrice(rental);

        Payment payment = paymentMapper.toModel(request);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmountToPay(rentPrice);

        try {
            Session session = stripeService.createStripeSession(rental, rentPrice);
            payment.setSessionUrl(session.getUrl());
            payment.setSessionId(session.getId());

            return paymentMapper.toDto(paymentRepository.save(payment));
        } catch (StripeException e) {
            throw new PaymentProcessingException("Can't create Stripe session: " + e.getMessage());
        }
    }

    @Override
    public Page<PaymentResponse> getPaymentsByUserId(Long userId, User user, Pageable pageable) {
        Long targetUserId = user.getRole() == User.Role.MANAGER
                ? userId
                : user.getId();

        return paymentRepository.findByRentalUserId(targetUserId, pageable)
                .map(paymentMapper::toDto);
    }

    @Override
    public void processSuccess(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Can't find payment by id: " + sessionId)
        );

        try {
            Session session = stripeService.getStripeSession(sessionId);
            String paymentStatus = session.getPaymentStatus();

            if ("paid".equals(paymentStatus)) {
                payment.setStatus(PaymentStatus.PAID);
                paymentRepository.save(payment);
            } else {
                throw new PaymentProcessingException("Payment not confirmed by Stripe. "
                        + "Current status: " + paymentStatus);
            }
        } catch (StripeException e) {
            throw new PaymentProcessingException(e.getMessage());
        }
    }

    @Override
    public void processCancel(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId).orElseThrow(
                () -> new EntityNotFoundException("Can't find payment by id: " + sessionId)
        );

        payment.setStatus(PaymentStatus.CANCELED);
        paymentRepository.save(payment);
    }

    private BigDecimal calculateTotalRentPrice(Rental rental) {
        long daysRented = ChronoUnit.DAYS.between(rental.getRentalDate(), rental.getReturnDate());
        return rental.getCar().getDailyFee()
                .multiply(BigDecimal.valueOf(daysRented));
    }
}
