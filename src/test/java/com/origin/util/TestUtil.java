package com.origin.util;

import com.origin.model.Car;
import com.origin.model.Payment;
import com.origin.model.Rental;
import com.origin.model.User;
import com.origin.model.enums.CarType;
import com.origin.model.enums.PaymentStatus;
import com.origin.model.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestUtil {
    public static User createUser() {
        return User.builder()
                .email("user@gmail.com")
                .firstName("John")
                .lastName("Wick")
                .password("$2a$08$eOLv98pkXuh5MFsEt2en7OWx8TVAlsCjJh1nAHw8q.e3X55u3ll96")
                .role(User.Role.CUSTOMER)
                .build();
    }

    public static Car createCar() {
        return Car.builder()
                .brand("Audi")
                .model("R8")
                .carType(CarType.SEDAN)
                .dailyFee(BigDecimal.valueOf(259.99))
                .build();
    }

    public static Rental createRental(User user, Car car) {
        return Rental.builder()
                .user(user)
                .car(car)
                .rentalDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(5))
                .build();
    }

    public static Payment createPayment(Rental rental) {
        return Payment.builder()
                .status(PaymentStatus.PENDING)
                .type(PaymentType.PAYMENT)
                .rental(rental)
                .sessionUrl("http://test-url")
                .sessionId("session_123")
                .amountToPay(BigDecimal.valueOf(99.99))
                .build();
    }
}
