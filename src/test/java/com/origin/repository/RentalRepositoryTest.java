package com.origin.repository;

import com.origin.model.Car;
import com.origin.model.Rental;
import com.origin.model.User;
import com.origin.repository.car.CarRepository;
import com.origin.repository.rental.RentalRepository;
import com.origin.repository.user.UserRepository;
import com.origin.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import java.time.LocalDate;
import java.util.List;

import static com.origin.util.TestConstants.CLEANUP_DB_PATH;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = CLEANUP_DB_PATH,
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RentalRepositoryTest {
    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    @Test
    @DisplayName("Should save rental and find by id")
    void shouldSaveAndFindById() {
        User user = userRepository.save(TestUtil.createUser());
        Car car = carRepository.save(TestUtil.createCar());

        Rental rental = TestUtil.createRental(user, car);

        Rental saved = rentalRepository.save(rental);

        Rental found = rentalRepository.findById(saved.getId())
                .orElseThrow(() -> new AssertionError("Rental not found"));

        assertEquals(saved.getId(), found.getId());
        assertEquals(user.getId(), found.getUser().getId());
        assertEquals(car.getId(), found.getCar().getId());
    }

    @Test
    @DisplayName("Should find active rental by user id")
    void shouldFindActiveRentalByUserId() {
        User user = userRepository.save(TestUtil.createUser());
        Car car = carRepository.save(TestUtil.createCar());

        Rental rental = TestUtil.createRental(user, car);

        rentalRepository.save(rental);

        var result = rentalRepository.findByUserIdAndActualReturnDateIsNull(
                user.getId(),
                org.springframework.data.domain.Pageable.unpaged()
        );

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should find overdue rentals")
    void shouldFindOverdueRentals() {
        User user = userRepository.save(TestUtil.createUser());
        Car car = carRepository.save(TestUtil.createCar());

        Rental rental = TestUtil.createRental(user, car);
        rental.setRentalDate(LocalDate.now().minusDays(10));
        rental.setReturnDate(LocalDate.now().minusDays(2));
        rental.setActualReturnDate(null);

        rentalRepository.save(rental);

        List<Rental> overdue = rentalRepository
                .findByReturnDateBeforeAndActualReturnDateIsNull(LocalDate.now());

        assertFalse(overdue.isEmpty());
        assertEquals(1, overdue.size());
    }

    @Test
    @DisplayName("Should find rental by id and user id")
    void shouldFindByIdAndUserId() {
        User user = userRepository.save(TestUtil.createUser());
        Car car = carRepository.save(TestUtil.createCar());

        Rental rental = TestUtil.createRental(user, car);
        rental.setReturnDate(LocalDate.now().plusDays(2));

        Rental saved = rentalRepository.save(rental);

        var found = rentalRepository.findByIdAndUserId(saved.getId(), user.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    @DisplayName("Should return empty when rental not found by user id")
    void shouldReturnEmptyWhenUserDoesNotOwnRental() {
        User user1 = userRepository.save(TestUtil.createUser());
        User user2 = TestUtil.createUser();
        user2.setEmail("anotheruser@gmail.com");

        userRepository.save(user2);

        Car car = carRepository.save(TestUtil.createCar());

        Rental rental = rentalRepository.save(TestUtil.createRental(user1, car));

        var result = rentalRepository.findByIdAndUserId(
                rental.getId(),
                user2.getId()
        );

        assertTrue(result.isEmpty());
    }
}