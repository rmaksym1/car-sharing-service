package com.origin.util;

import com.origin.model.Car;
import com.origin.model.User;
import com.origin.model.enums.CarType;

import java.math.BigDecimal;

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
}
