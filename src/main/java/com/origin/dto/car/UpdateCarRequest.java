package com.origin.dto.car;

import com.origin.model.enums.CarType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateCarRequest(
        @Size(min = 1, max = 30, message = "Model name must be between 1 and 30 characters long!")
        String model,
        @Size(min = 1, max = 30, message = "Brand name must be between 1 and 30 characters long!")
        String brand,
        @NotNull(message = "Car type cannot be null!")
        CarType carType,
        @DecimalMin(value = "0.01", message = "Daily fee must be greater than 0!")
        BigDecimal dailyFee
) {}
