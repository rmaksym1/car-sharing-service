package com.origin.dto.notification;

import com.origin.model.Car;
import com.origin.model.Rental;
import com.origin.model.User;

public record Context(
        Rental rental,
        Car car,
        User user
) {}
