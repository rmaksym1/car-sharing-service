package com.origin.service;

import com.origin.dto.car.CarResponse;
import com.origin.dto.car.CreateCarRequest;
import com.origin.dto.car.UpdateCarInventoryRequest;
import com.origin.dto.car.UpdateCarRequest;
import com.origin.exception.EntityNotFoundException;
import com.origin.mapper.CarMapper;
import com.origin.model.Car;
import com.origin.repository.car.CarRepository;
import com.origin.service.impl.CarServiceImpl;
import com.origin.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {
    private static final Pageable pageable = PageRequest.of(0, 10);
    private static final Long ID = 1L;
    private static final Long INCORRECT_ID = 999L;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarServiceImpl carService;

    @Test
    @DisplayName("Should save a car successfully")
    void saveCar_ReturnsResponseDto() {
        Car car = TestUtil.createCar();
        CreateCarRequest carRequest = TestUtil.createCarRequest();
        CarResponse carResponse = TestUtil.createCarResponse();

        when(carMapper.toModel(carRequest)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(carResponse);

        CarResponse actual = carService.saveCar(carRequest);

        assertEquals(carResponse, actual);
        verify(carMapper).toModel(carRequest);
        verify(carRepository).save(car);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Should save a car successfully")
    void getAll_ReturnsPageOfResponseDtos() {
        Car car = TestUtil.createCar();
        CarResponse carResponse = TestUtil.createCarResponse();
        Page<Car> carPage = new PageImpl<>(List.of(car), pageable, 1);

        when(carRepository.findAll(pageable)).thenReturn(carPage);
        when(carMapper.toDto(car)).thenReturn(carResponse);

        Page<CarResponse> actual = carService.getAll(pageable);

        assertEquals(carPage.getSize(), actual.getSize());
        verify(carRepository).findAll(pageable);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Should return a car by id successfully")
    void getCarById_ReturnsResponseDto() {
        Car car = TestUtil.createCar();
        CarResponse carResponse = TestUtil.createCarResponse();

        when(carRepository.findById(ID)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(carResponse);

        CarResponse actual = carService.getCarById(ID);

        assertEquals(carResponse, actual);
        verify(carRepository).findById(ID);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Should throw an exception if car not found")
    void getCarByIncorrectId_ThrowsException() {
        when(carRepository.findById(INCORRECT_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> carService.getCarById(INCORRECT_ID)
        );
        verifyNoInteractions(carMapper);
    }

    @Test
    @DisplayName("Should update a car by id successfully")
    void updateCarById_ReturnsResponseDto() {
        Car car = TestUtil.createCar();
        CarResponse carResponse = TestUtil.createCarResponse();
        UpdateCarRequest carRequest = TestUtil.createUpdateCarRequest();

        when(carRepository.findById(ID)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(carResponse);

        CarResponse actual = carService.updateCarById(carRequest, ID);

        assertEquals(carResponse, actual);
        verify(carRepository).findById(ID);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Should throw an exception if updatable car with incorrect id is not found")
    void updateIncorrectCarById_ThrowsException() {
        UpdateCarRequest updateCarRequest = TestUtil.createUpdateCarRequest();

        when(carRepository.findById(INCORRECT_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> carService.updateCarById(updateCarRequest, INCORRECT_ID)
        );
        verifyNoInteractions(carMapper);
    }

    @Test
    @DisplayName("Should update car inventory successfully")
    void updateCarInventoryById_ShouldReturnUpdatedCarResponse() {
        Car car = TestUtil.createCar();

        UpdateCarInventoryRequest request = TestUtil.createUpdateCarInventoryRequest();
        CarResponse expectedResponse = TestUtil.createCarResponse();

        when(carRepository.findById(ID)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(expectedResponse);

        CarResponse actualResponse = carService.updateCarInventoryById(request, ID);

        assertNotNull(actualResponse);
        assertEquals(10, car.getInventory());
        assertEquals(expectedResponse, actualResponse);

        verify(carRepository).findById(ID);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Should throw an exception if car with incorrect id is not found")
    void updateIncorrectCarInventoryById_ThrowsException() {
        UpdateCarInventoryRequest updateCarRequest = TestUtil.createUpdateCarInventoryRequest();

        when(carRepository.findById(INCORRECT_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> carService.updateCarInventoryById(updateCarRequest, INCORRECT_ID)
        );
        verifyNoInteractions(carMapper);
    }

    @Test
    @DisplayName("Should successfully delete a car by id")
    void deleteCarById_ShouldDeleteCar() {
        Car car = TestUtil.createCar();

        when(carRepository.findById(ID)).thenReturn(Optional.of(car));

        carService.deleteCarById(ID);

        verify(carRepository, times(1)).findById(ID);
        verify(carRepository, times(1)).delete(car);
    }
}
