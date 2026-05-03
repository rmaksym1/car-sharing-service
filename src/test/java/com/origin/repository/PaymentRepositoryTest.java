package com.origin.repository;

import com.origin.model.Car;
import com.origin.model.Payment;
import com.origin.model.Rental;
import com.origin.model.User;
import com.origin.model.enums.PaymentStatus;
import com.origin.repository.car.CarRepository;
import com.origin.repository.payment.PaymentRepository;
import com.origin.repository.rental.RentalRepository;
import com.origin.repository.user.UserRepository;
import com.origin.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import static com.origin.util.TestConstants.CLEANUP_DB_PATH;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = CLEANUP_DB_PATH,
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    private Payment createPayment() {
        User user = userRepository.save(TestUtil.createUser());
        Car car = carRepository.save(TestUtil.createCar());

        Rental rental = rentalRepository.save(TestUtil.createRental(user, car));

        Payment payment = TestUtil.createPayment(rental);

        return paymentRepository.save(payment);
    }

    @Test
    @DisplayName("Should find payment by session id")
    void shouldFindBySessionId() {
        Payment saved = createPayment();

        var found = paymentRepository.findBySessionId(saved.getSessionId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    @DisplayName("Should check exists by rental and status")
    void shouldCheckExistsByRentalAndStatus() {
        Payment payment = createPayment();

        boolean exists = paymentRepository.existsByRentalAndStatus(
                payment.getRental(),
                PaymentStatus.PENDING
        );

        assertTrue(exists);
    }

    @Test
    @DisplayName("Should check exists by user and status")
    void shouldCheckExistsByUserAndStatus() {
        Payment payment = createPayment();

        boolean exists = paymentRepository.existsByRentalUserAndStatus(
                payment.getRental().getUser(),
                PaymentStatus.PENDING
        );

        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return payments by user id")
    void shouldFindByUserId() {
        Payment payment = createPayment();

        var page = paymentRepository.findByRentalUserId(
                payment.getRental().getUser().getId(),
                org.springframework.data.domain.Pageable.unpaged()
        );

        assertEquals(1, page.getTotalElements());
    }

    @Test
    @DisplayName("Should return empty when session id not found")
    void shouldReturnEmptyWhenSessionIdNotFound() {
        var result = paymentRepository.findBySessionId("invalid");

        assertTrue(result.isEmpty());
    }
}