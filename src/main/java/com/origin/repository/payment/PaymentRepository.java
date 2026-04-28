package com.origin.repository.payment;

import com.origin.model.Payment;
import com.origin.model.Rental;
import com.origin.model.User;
import com.origin.model.enums.PaymentStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByRentalUserId(Long userId, Pageable pageable);

    Optional<Payment> findBySessionId(String sessionId);

    boolean existsByRentalAndStatus(Rental rental, PaymentStatus paymentStatus);

    boolean existsByRentalUserAndStatus(User user, PaymentStatus paymentStatus);
}
