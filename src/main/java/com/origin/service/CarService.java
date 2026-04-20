package com.origin.service;

import com.origin.dto.car.CarResponse;
import com.origin.dto.car.CreateCarRequest;
import com.origin.dto.car.UpdateCarInventoryRequest;
import com.origin.dto.car.UpdateCarRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarResponse saveCar(CreateCarRequest carRequest);

    Page<CarResponse> getAll(Pageable pageable);

    CarResponse getCarById(Long id);

    CarResponse updateCarById(UpdateCarRequest updateCarRequest, Long id);

    CarResponse updateCarInventoryById(
            UpdateCarInventoryRequest updateCarInventoryRequest,
            Long id
    );

    void deleteCarById(Long id);
}
