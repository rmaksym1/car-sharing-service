package com.origin.service.impl;

import com.origin.dto.rental.CreateRentalRequest;
import com.origin.dto.rental.RentalResponse;
import com.origin.exception.EntityNotFoundException;
import com.origin.exception.car.CarAlreadyReturnedException;
import com.origin.exception.car.CarNotAvailableException;
import com.origin.mapper.RentalMapper;
import com.origin.model.Car;
import com.origin.model.Rental;
import com.origin.model.User;
import com.origin.repository.car.CarRepository;
import com.origin.repository.rental.RentalRepository;
import com.origin.service.RentalService;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final CarRepository carRepository;
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;

    @Override
    @Transactional
    public RentalResponse save(User user, CreateRentalRequest request) {
        Car car = carRepository.findById(request.carId()).orElseThrow(
                () -> new EntityNotFoundException("Car by id: "
                        + request.carId() + " not found")
        );

        if (car.getInventory() < 1) {
            throw new CarNotAvailableException("Car with id: "
                    + car.getId() + " is out of stock");
        }

        Rental rental = rentalMapper.toModel(request);
        rental.setCar(car);
        rental.setUser(user);
        rental.setRentalDate(LocalDate.now());

        car.setInventory(car.getInventory() - 1);
        return rentalMapper.toDto(rentalRepository.save(rental));
    }

    @Override
    public Page<RentalResponse> getRentals(
            Long userId,
            Boolean isActive,
            User user,
            Pageable pageable) {
        Long targetUserId = user.getRole() == User.Role.MANAGER
                ? userId
                : user.getId();

        if (isActive == null) {
            return rentalRepository.findByUserId(targetUserId, pageable)
                    .map(rentalMapper::toDto);
        }

        return isActive
                ? rentalRepository.findByUserIdAndActualReturnDateIsNull(targetUserId, pageable)
                .map(rentalMapper::toDto)
                : rentalRepository.findByUserIdAndActualReturnDateIsNotNull(targetUserId, pageable)
                .map(rentalMapper::toDto);
    }

    @Override
    public RentalResponse getRentalById(User user, Long id) {
        return rentalMapper.toDto(getRentalByUserRoleAndId(user, id));
    }

    @Override
    @Transactional
    public RentalResponse setActualReturnDate(User user, Long id) {
        Rental rental = getRentalByUserRoleAndId(user, id);

        if (rental.getActualReturnDate() != null) {
            throw new CarAlreadyReturnedException("Car by id " + id + " is already returned!");
        }
        rental.setActualReturnDate(LocalDate.now());

        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);
        return rentalMapper.toDto(rental);
    }

    private Rental getRentalByUserRoleAndId(User user, Long id) {
        return user.getRole() == User.Role.MANAGER
                ? rentalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rental by id: "
                        + id + " not found!")
                )
                : rentalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Rental by id: "
                                + id + " not found!")
                );
    }
}
