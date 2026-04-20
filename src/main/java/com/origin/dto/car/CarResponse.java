package com.origin.dto.car;

import com.origin.model.enums.CarType;
import java.math.BigDecimal;

public record CarResponse(
        Long id,
        String model,
        String brand,
        CarType carType,
        int inventory,
        BigDecimal dailyFee
) {}
