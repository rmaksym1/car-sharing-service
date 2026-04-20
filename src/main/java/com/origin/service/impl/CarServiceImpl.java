package com.origin.service.impl;

import com.origin.dto.car.CarResponse;
import com.origin.dto.car.CreateCarRequest;
import com.origin.dto.car.UpdateCarInventoryRequest;
import com.origin.dto.car.UpdateCarRequest;
import com.origin.exception.EntityNotFoundException;
import com.origin.mapper.CarMapper;
import com.origin.model.Car;
import com.origin.repository.car.CarRepository;
import com.origin.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarResponse saveCar(CreateCarRequest carRequest) {
        return carMapper.toDto(carRepository.save(carMapper.toModel(carRequest)));
    }

    @Override
    public Page<CarResponse> getAll(Pageable pageable) {
        return carRepository.findAll(pageable)
                .map(carMapper::toDto);
    }

    @Override
    public CarResponse getCarById(Long id) {
        Car car = findCarOrThrow(id);

        return carMapper.toDto(car);
    }

    @Transactional
    @Override
    public CarResponse updateCarById(UpdateCarRequest request, Long id) {
        Car car = findCarOrThrow(id);

        carMapper.updateCar(request, car);

        return carMapper.toDto(car);
    }

    @Transactional
    @Override
    public CarResponse updateCarInventoryById(UpdateCarInventoryRequest request, Long id) {
        Car car = findCarOrThrow(id);

        car.setInventory(request.inventory());

        return carMapper.toDto(car);
    }

    @Override
    public void deleteCarById(Long id) {
        carRepository.deleteById(id);
    }

    public Car findCarOrThrow(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car by id: " + id + " not found!")
                );
    }
}
