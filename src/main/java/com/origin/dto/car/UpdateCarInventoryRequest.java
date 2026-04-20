package com.origin.dto.car;

import jakarta.validation.constraints.PositiveOrZero;

public record UpdateCarInventoryRequest(
        @PositiveOrZero(message = "Inventory must be positive or zero!")
        int inventory
) {}
