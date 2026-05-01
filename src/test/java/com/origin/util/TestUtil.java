package com.origin.util;

import com.origin.dto.car.CarResponse;
import com.origin.dto.car.CreateCarRequest;
import com.origin.dto.car.UpdateCarInventoryRequest;
import com.origin.dto.car.UpdateCarRequest;
import com.origin.dto.rental.CreateRentalRequest;
import com.origin.dto.rental.RentalResponse;
import com.origin.dto.user.UserResponse;
import com.origin.dto.user.auth.UserLoginRequest;
import com.origin.dto.user.auth.UserRegistrationRequest;
import com.origin.dto.user.profile.UpdateUserRoleRequest;
import com.origin.dto.user.profile.UserInfoUpdateRequest;
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

    public static UserResponse createUserResponse() {
        return new UserResponse(1L,
                "user@gmail.com",
                "John",
                "Wick",
                User.Role.CUSTOMER);
    }

    public static UserInfoUpdateRequest createUserInfoUpdateRequest() {
        return new UserInfoUpdateRequest("John", "Wick");
    }

    public static UserRegistrationRequest createUserRegistrationRequest() {
        return new UserRegistrationRequest("user@gmail.com",
                "John",
                "Wick",
                "password",
                "password");
    }

    public static UserLoginRequest createUserLoginRequest() {
        return new UserLoginRequest("user@gmail.com",
                "password");
    }

    public static Car createCar() {
        return Car.builder()
                .brand("Audi")
                .model("R8")
                .carType(CarType.SEDAN)
                .dailyFee(BigDecimal.valueOf(249.99))
                .inventory(10)
                .build();
    }

    public static CreateCarRequest createCarRequest() {
        return new CreateCarRequest(
                "Audi",
                "R8",
                CarType.SEDAN,
                5,
                BigDecimal.valueOf(249.99)
        );
    }

    public static CarResponse createCarResponse() {
        return new CarResponse(1L,
                "Audi",
                "R8",
                CarType.SEDAN,
                15,
                BigDecimal.valueOf(249.99)
        );
    }

    public static UpdateCarInventoryRequest createUpdateCarInventoryRequest() {
        return new UpdateCarInventoryRequest(10);
    }

    public static UpdateCarRequest createUpdateCarRequest() {
        return new UpdateCarRequest("Audi",
                "R8",
                CarType.SEDAN,
                BigDecimal.valueOf(249.99)
        );
    }

    public static Rental createRental(User user, Car car) {
        return Rental.builder()
                .user(user)
                .car(car)
                .rentalDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(5))
                .build();
    }

    public static RentalResponse createRentalResponse(CarResponse carResponse) {
        return new RentalResponse(
                1L,
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                null, carResponse,
                1L
        );
    }

    public static CreateRentalRequest createRentalRequest() {
        return new CreateRentalRequest(
                1L,
                LocalDate.now().plusDays(5)
        );
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

    public static UpdateUserRoleRequest createUpdateUserRoleRequest() {
        return new UpdateUserRoleRequest(User.Role.MANAGER);
    }
}
