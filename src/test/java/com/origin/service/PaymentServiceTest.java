package com.origin.service;

import com.origin.dto.payment.CreatePaymentRequest;
import com.origin.dto.payment.PaymentResponse;
import com.origin.exception.EntityNotFoundException;
import com.origin.exception.payment.PaymentProcessingException;
import com.origin.mapper.PaymentMapper;
import com.origin.model.Car;
import com.origin.model.Payment;
import com.origin.model.Rental;
import com.origin.model.User;
import com.origin.model.enums.PaymentStatus;
import com.origin.model.enums.PaymentType;
import com.origin.notification.impl.NotificationServiceImpl;
import com.origin.repository.payment.PaymentRepository;
import com.origin.repository.rental.RentalRepository;
import com.origin.service.payment.PaymentServiceImpl;
import com.origin.service.payment.StripeService;
import com.origin.util.TestUtil;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    private static final Pageable pageable = PageRequest.of(0, 10);

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private StripeService stripeService;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private NotificationServiceImpl notificationService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private static final Long ID = 1L;

    @ParameterizedTest
    @MethodSource("paymentProvider")
    @DisplayName("Should create payment depending on type")
    void createPayment_ByType_ReturnsResponse(
            PaymentType type,
            Rental rental,
            BigDecimal expectedAmount
    ) throws StripeException {
        CreatePaymentRequest request = TestUtil.createPaymentRequest(type);
        Payment payment = TestUtil.createPayment(rental);
        Session stripeSession = mock(Session.class);
        PaymentResponse expected = TestUtil.createPaymentResponse();

        when(rentalRepository.findById(ID)).thenReturn(Optional.of(rental));

        if (type == PaymentType.PAYMENT) {
            when(paymentRepository.existsByRentalAndStatus(rental, PaymentStatus.PAID)).thenReturn(false);
            when(paymentRepository.existsByRentalAndStatus(rental, PaymentStatus.PENDING)).thenReturn(false);
            when(stripeSession.getUrl()).thenReturn("http://stripe.url");
            when(stripeSession.getId()).thenReturn("session_id");
        }

        when(paymentMapper.toModel(request)).thenReturn(payment);

        when(stripeService.createStripeSession(
                eq(rental),
                any(BigDecimal.class),
                eq(type)
        )).thenReturn(stripeSession);

        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(expected);

        PaymentResponse actual = paymentService.createPayment(request);

        assertEquals(expected, actual);
        assertEquals(0, expectedAmount.compareTo(payment.getAmountToPay()));
        verify(stripeService).createStripeSession(eq(rental), any(BigDecimal.class), eq(type));
    }

    @Test
    @DisplayName("Should throw an exception when trying to save a payment with invalid rental")
    void savePaymentWithInvalidRental_ThrowsException() {
        CreatePaymentRequest request = TestUtil.createPaymentRequest(PaymentType.PAYMENT);

        when(rentalRepository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> paymentService.createPayment(request)
        );
        verify(rentalRepository).findById(ID);
        verifyNoInteractions(paymentMapper, paymentRepository);
    }

    @Test
    @DisplayName("Should throw an exception when trying to create a fine with no overdue")
    void saveFinePaymentWithNoOverdue_ThrowsException() {
        CreatePaymentRequest request = TestUtil.createPaymentRequest(PaymentType.FINE);
        Rental rental = TestUtil.createRental(TestUtil.createUser(), TestUtil.createCar());
        rental.setActualReturnDate(rental.getReturnDate());

        when(rentalRepository.findById(ID)).thenReturn(Optional.of(rental));
        when(paymentRepository.existsByRentalAndStatus(rental, PaymentStatus.PENDING)).thenReturn(false);

        assertThrows(PaymentProcessingException.class,
                () -> paymentService.createPayment(request)
        );
        verify(rentalRepository).findById(ID);
        verifyNoInteractions(paymentMapper);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    @DisplayName("Should throw an exception when trying to create a fine payment with null actual return date")
    void saveFinePayment_ActualReturnDateNull_ThrowsException() {
        CreatePaymentRequest request = TestUtil.createPaymentRequest(PaymentType.FINE);
        Rental rental = TestUtil.createRental(TestUtil.createUser(), new Car());

        when(rentalRepository.findById(ID)).thenReturn(Optional.of(rental));
        when(paymentRepository.existsByRentalAndStatus(rental, PaymentStatus.PENDING)).thenReturn(false);

        assertThrows(PaymentProcessingException.class,
                () -> paymentService.createPayment(request)
        );

        verify(rentalRepository).findById(ID);
        verifyNoInteractions(paymentMapper, stripeService);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw an exception when trying to save a payment with already paid rental")
    void savePayment_AlreadyPaidRental_ThrowsException() {
        CreatePaymentRequest request = TestUtil.createPaymentRequest(PaymentType.PAYMENT);
        Rental rental = TestUtil.createRental(TestUtil.createUser(), new Car());

        when(rentalRepository.findById(ID)).thenReturn(Optional.of(rental));
        when(paymentRepository.existsByRentalAndStatus(rental, PaymentStatus.PAID)).thenReturn(true);

        assertThrows(PaymentProcessingException.class,
                () -> paymentService.createPayment(request)
        );

        verify(rentalRepository).findById(ID);
        verifyNoInteractions(paymentMapper, stripeService);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw an exception when trying to save a payment with active payment session")
    void savePayment_ActivePaymentSession_ThrowsException() {
        CreatePaymentRequest request = TestUtil.createPaymentRequest(PaymentType.PAYMENT);
        Rental rental = TestUtil.createRental(TestUtil.createUser(), TestUtil.createCar());

        when(rentalRepository.findById(ID)).thenReturn(Optional.of(rental));
        when(paymentRepository.existsByRentalAndStatus(rental, PaymentStatus.PAID)).thenReturn(false);
        when(paymentRepository.existsByRentalAndStatus(rental, PaymentStatus.PENDING)).thenReturn(true);

        assertThrows(PaymentProcessingException.class,
                () -> paymentService.createPayment(request)
        );

        verify(rentalRepository).findById(ID);
        verifyNoInteractions(paymentMapper, stripeService);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw an exception when trying to save a payment with invalid rental")
    void savePayment_StripeApiError_ThrowsException() throws StripeException {
        CreatePaymentRequest request = TestUtil.createPaymentRequest(PaymentType.PAYMENT);
        Rental rental = TestUtil.createRental(TestUtil.createUser(), TestUtil.createCar());
        Payment payment = TestUtil.createPayment(rental);

        when(rentalRepository.findById(ID)).thenReturn(Optional.of(rental));
        when(paymentRepository.existsByRentalAndStatus(rental, PaymentStatus.PAID)).thenReturn(false);
        when(paymentRepository.existsByRentalAndStatus(rental, PaymentStatus.PENDING)).thenReturn(false);
        when(paymentMapper.toModel(request)).thenReturn(payment);
        when(stripeService.createStripeSession(eq(rental), any(BigDecimal.class), eq(PaymentType.PAYMENT))).thenThrow(
                new InvalidRequestException("Error", null, null, null, null, null)
        );

        assertThrows(PaymentProcessingException.class,
                () -> paymentService.createPayment(request)
        );
        verify(rentalRepository).findById(ID);
        verifyNoMoreInteractions(paymentMapper);
        verify(paymentRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({
            "MANAGER, true",
            "CUSTOMER, false"
    })
    @DisplayName("Should return user's payments for user, any user payments for manager")
    void getPaymentsByRole_ReturnsPage(
            User.Role role,
            boolean isManager
    ) {
        User user = TestUtil.createUser();
        user.setId(1L);
        user.setRole(role);

        Payment payment = TestUtil.createPayment(TestUtil.createRental(user, new Car()));
        PaymentResponse paymentResponse = TestUtil.createPaymentResponse();

        Long expectedTargetId = isManager ? ID : user.getId();

        when(paymentRepository.findByRentalUserId(expectedTargetId, pageable))
                .thenReturn(new PageImpl<>(List.of(payment)));
        when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);

        Page<PaymentResponse> actual = paymentService.getPaymentsByUserId(ID, user, pageable);

        assertEquals(1, actual.getContent().size());
        verify(paymentRepository).findByRentalUserId(expectedTargetId, pageable);
    }

    @Test
    @DisplayName("Should update status to PAID on successful stripe session")
    void processSuccess_ValidSession_UpdatesStatus() throws StripeException {
        String sessionId = "valid_session";
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        Session stripeSession = mock(Session.class);

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        when(stripeService.getStripeSession(sessionId)).thenReturn(stripeSession);
        when(stripeSession.getPaymentStatus()).thenReturn("paid");

        paymentService.processSuccess(sessionId);

        assertEquals(PaymentStatus.PAID, payment.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("Should throw an exception on invalid session id")
    void processSuccess_InvalidSessionId_ThrowsException() {
        String sessionId = "invalid_session";
        Payment payment = TestUtil.createPayment(new Rental());
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> paymentService.processSuccess(sessionId)
        );

        verifyNoInteractions(stripeService);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    @DisplayName("Should throw an exception on invalid session status")
    void processSuccess_InvalidSessionStatus_ThrowsException() throws StripeException {
        String sessionId = "valid_session";
        Payment payment = TestUtil.createPayment(new Rental());
        payment.setStatus(PaymentStatus.PENDING);
        Session stripeSession = mock(Session.class);
        stripeSession.setPaymentStatus("invalid_status");

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        when(stripeService.getStripeSession(sessionId)).thenReturn(stripeSession);

        assertThrows(PaymentProcessingException.class,
                () -> paymentService.processSuccess(sessionId)
        );

        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    @DisplayName("Should throw an exception when session is not found")
    void processSuccess_NoSession_ThrowsException() throws StripeException {
        String sessionId = "valid_session";
        Payment payment = TestUtil.createPayment(new Rental());
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        when(stripeService.getStripeSession(sessionId)).thenThrow(new InvalidRequestException("Error", null, null, null, null, null));

        assertThrows(PaymentProcessingException.class,
                () -> paymentService.processSuccess(sessionId)
        );

        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    @DisplayName("Should update status to CANCELED on processCancel")
    void processCancel_ValidSession_UpdatesStatus() {
        String sessionId = "cancel_session";
        Payment payment = TestUtil.createPayment(new Rental());

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));

        paymentService.processCancel(sessionId);

        assertEquals(PaymentStatus.CANCELED, payment.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("Should throw an exception when session id is not valid on processCancel")
    void processCancel_InvalidSessionId_UpdatesStatus() {
        String sessionId = "invalid_session";

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> paymentService.processCancel(sessionId)
        );

        verify(paymentRepository).findBySessionId(sessionId);
    }

    static Stream<Arguments> paymentProvider() {
        Rental regularRental = TestUtil.createRental(TestUtil.createUser(), TestUtil.createCar());
        regularRental.setRentalDate(LocalDate.now());
        regularRental.setReturnDate(LocalDate.now().plusDays(2));

        Rental fineRental = TestUtil.createRental(TestUtil.createUser(), TestUtil.createCar());
        fineRental.setReturnDate(LocalDate.now().minusDays(2));
        fineRental.setActualReturnDate(LocalDate.now());

        return Stream.of(
                Arguments.of(PaymentType.PAYMENT, regularRental, calculateRegularPayment(regularRental)),
                Arguments.of(PaymentType.FINE, fineRental, calculateFine(fineRental))
        );
    }

    static BigDecimal calculateFine(Rental rental) {
        BigDecimal daysOverdue = BigDecimal.valueOf(
                Math.max(0, ChronoUnit.DAYS.between(
                        rental.getReturnDate(),
                        rental.getActualReturnDate()
                ))
        );

        return rental.getCar()
                .getDailyFee()
                .multiply(daysOverdue)
                .multiply(BigDecimal.valueOf(1.15));
    }

    static BigDecimal calculateRegularPayment(Rental rental) {
        long daysRented = ChronoUnit.DAYS.between(rental.getRentalDate(), rental.getReturnDate());
        return rental.getCar().getDailyFee()
                .multiply(BigDecimal.valueOf(daysRented));
    }
}
