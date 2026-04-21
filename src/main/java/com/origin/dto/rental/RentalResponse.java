package com.origin.dto.rental;

import com.origin.dto.car.CarResponse;
import java.time.LocalDate;

public record RentalResponse(
        Long id,
        LocalDate rentalDate,
        LocalDate returnDate,
        LocalDate actualReturnDate,
        CarResponse car,
        Long userId
) {}
