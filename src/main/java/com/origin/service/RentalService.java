package com.origin.service;

import com.origin.dto.rental.CreateRentalRequest;
import com.origin.dto.rental.RentalResponse;
import com.origin.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {
    RentalResponse save(User user, CreateRentalRequest createRentalRequest);

    Page<RentalResponse> getRentals(Long userId, Boolean isActive, User user, Pageable pageable);

    RentalResponse getRentalById(User user, Long id);

    RentalResponse setActualReturnDate(User user, Long id);
}
