package com.origin.service;

import com.origin.dto.rental.CreateRentalRequest;
import com.origin.dto.rental.RentalResponse;
import com.origin.exception.EntityNotFoundException;
import com.origin.exception.car.CarAlreadyReturnedException;
import com.origin.exception.car.CarNotAvailableException;
import com.origin.mapper.RentalMapper;
import com.origin.model.Car;
import com.origin.model.Rental;
import com.origin.model.User;
import com.origin.model.enums.PaymentStatus;
import com.origin.notification.NotificationService;
import com.origin.repository.car.CarRepository;
import com.origin.repository.payment.PaymentRepository;
import com.origin.repository.rental.RentalRepository;
import com.origin.service.impl.RentalServiceImpl;
import com.origin.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {
    private static final Pageable pageable = PageRequest.of(0, 10);
    private static final Long ID = 1L;
    private static final Long INCORRECT_ID = 999L;

    @Mock
    private CarRepository carRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private RentalServiceImpl rentalService;

    @Test
    @DisplayName("Should save rental successfully")
    void save_ValidRequest_ReturnsResponse() {
        User user = TestUtil.createUser();
        Car car = TestUtil.createCar();

        CreateRentalRequest request = TestUtil.createRentalRequest();
        Rental rental = TestUtil.createRental(user, car);
        RentalResponse expected = TestUtil.createRentalResponse(
                TestUtil.createCarResponse()
        );

        when(rentalMapper.toModel(request)).thenReturn(rental);
        when(carRepository.findById(ID)).thenReturn(Optional.of(car));
        when(paymentRepository.existsByRentalUserAndStatus(user, PaymentStatus.PENDING))
                .thenReturn(false);
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(expected);

        RentalResponse actual = rentalService.save(user, request);

        assertNotNull(actual);
        assertEquals(9, car.getInventory());
        assertEquals(expected, actual);
        verify(notificationService).sendRentalMessage(rental);
        verify(rentalRepository).save(rental);
    }

    @Test
    @DisplayName("Should throw an exception when trying to save rental with non existent car")
    void saveRentalWithIncorrectCar_ThrowsException() {
        User user = TestUtil.createUser();
        CreateRentalRequest request = TestUtil.createRentalRequest();
        Rental rental = TestUtil.createRental(user, new Car());

        when(rentalMapper.toModel(request)).thenReturn(rental);
        when(carRepository.findById(request.carId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> rentalService.save(user, request)
        );

        verifyNoInteractions(notificationService);
        verifyNoInteractions(rentalRepository);
        verifyNoMoreInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Should throw an exception when trying to "
            + "save rental with not available car")
    void saveRentalWithNotAvailableCar_ThrowsException() {
        User user = TestUtil.createUser();
        CreateRentalRequest request = TestUtil.createRentalRequest();
        Rental rental = TestUtil.createRental(user, new Car());
        Car car = TestUtil.createCar();
        car.setInventory(0);

        when(rentalMapper.toModel(request)).thenReturn(rental);
        when(carRepository.findById(request.carId())).thenReturn(Optional.of(car));

        assertThrows(CarNotAvailableException.class,
                () -> rentalService.save(user, request)
        );

        verifyNoInteractions(notificationService);
        verifyNoInteractions(rentalRepository);
        verifyNoMoreInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Should throw an exception when trying to "
            + "save rental with existing payment")
    void saveRentalWithExistingPayment_ThrowsException() {
        User user = TestUtil.createUser();
        CreateRentalRequest request = TestUtil.createRentalRequest();
        Rental rental = TestUtil.createRental(user, new Car());
        Car car = TestUtil.createCar();

        when(rentalMapper.toModel(request)).thenReturn(rental);
        when(carRepository.findById(request.carId())).thenReturn(Optional.of(car));
        when(paymentRepository.existsByRentalUserAndStatus(user, PaymentStatus.PENDING))
                .thenReturn(true);

        assertThrows(CarNotAvailableException.class,
                () -> rentalService.save(user, request)
        );

        verifyNoInteractions(notificationService);
        verifyNoInteractions(rentalRepository);
        verifyNoMoreInteractions(rentalMapper);
    }

    @ParameterizedTest
    @CsvSource({
            "MANAGER, true",
            "CUSTOMER, false"
    })
    @DisplayName("Should return user's rentals depending on user's role")
    void getRentals_ByRole_ReturnsPage(
            User.Role role,
            boolean isManager
    ) {
        User user = TestUtil.createUser();
        user.setId(ID);
        user.setRole(role);
        Rental rental = TestUtil.createRental(user, TestUtil.createCar());
        RentalResponse responseDto = TestUtil.createRentalResponse(TestUtil.createCarResponse());
        Page<Rental> rentals = new PageImpl<>(List.of(rental));

        when(rentalRepository.findByUserIdAndActualReturnDateIsNull(ID, pageable)).thenReturn(rentals);
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        Page<RentalResponse> actual = rentalService.getRentals(ID, true, user, pageable);

        assertEquals(1, actual.getContent().size());
        if (isManager) {
            verify(rentalRepository).findByUserIdAndActualReturnDateIsNull(ID, pageable);
        } else {
            verify(rentalRepository).findByUserIdAndActualReturnDateIsNull(user.getId(), pageable);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "MANAGER, true",
            "CUSTOMER, false"
    })
    @DisplayName("Should return unactive rentals depending on user role")
    void getNotActiveRentals_ByRole_ReturnsPage(
            User.Role role,
            boolean isManager
    ) {
        User user = TestUtil.createUser();
        user.setId(ID);
        user.setRole(role);
        Rental rental = TestUtil.createRental(user, TestUtil.createCar());
        RentalResponse responseDto = TestUtil.createRentalResponse(TestUtil.createCarResponse());
        Page<Rental> rentals = new PageImpl<>(List.of(rental));

        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        if (role == User.Role.MANAGER) {
            when(rentalRepository.findByUserIdAndActualReturnDateIsNotNull(ID, pageable))
                    .thenReturn(rentals);
        } else {
            when(rentalRepository.findByUserIdAndActualReturnDateIsNotNull(user.getId(), pageable))
                    .thenReturn(rentals);
        }

        Page<RentalResponse> actual = rentalService.getRentals(ID, false, user, pageable);

        assertEquals(1, actual.getContent().size());
        if (isManager) {
            verify(rentalRepository).findByUserIdAndActualReturnDateIsNotNull(ID, pageable);
        } else {
            verify(rentalRepository).findByUserIdAndActualReturnDateIsNotNull(user.getId(), pageable);
        }
        verify(rentalMapper).toDto(rental);
    }

    @Test
    @DisplayName("Should return all rentals for any user when is active is not specified")
    void getRentalsWithUnspecifiedIsActive_AnyRole_ReturnsPage() {
        User user = TestUtil.createUser();
        user.setId(ID);
        user.setRole(User.Role.MANAGER);
        Rental rental = TestUtil.createRental(user, TestUtil.createCar());
        RentalResponse responseDto = TestUtil.createRentalResponse(TestUtil.createCarResponse());
        Page<Rental> rentals = new PageImpl<>(List.of(rental));

        when(rentalRepository.findByUserId(ID, pageable)).thenReturn(rentals);
        when(rentalMapper.toDto(rental)).thenReturn(responseDto);

        Page<RentalResponse> actual = rentalService.getRentals(ID, null, user, pageable);

        assertEquals(1, actual.getContent().size());
        verify(rentalRepository).findByUserId(ID, pageable);
    }

    @ParameterizedTest
    @CsvSource({
            "MANAGER, true",
            "CUSTOMER, false"
    })
    @DisplayName("Should return rental by id depending on user role")
    void getRentalById_ByRole_ReturnsResponse(
            User.Role role,
            boolean isManager
    ) {
        User user = TestUtil.createUser();
        user.setId(ID);
        user.setRole(role);

        Car car = TestUtil.createCar();
        Rental rental = TestUtil.createRental(user, car);
        RentalResponse expected = TestUtil.createRentalResponse(TestUtil.createCarResponse());

        if (isManager) {
            when(rentalRepository.findById(ID)).thenReturn(Optional.of(rental));
        } else {
            when(rentalRepository.findByIdAndUserId(ID, user.getId()))
                    .thenReturn(Optional.of(rental));
        }

        when(rentalMapper.toDto(rental)).thenReturn(expected);

        RentalResponse actual = rentalService.getRentalById(user, ID);

        assertEquals(expected, actual);

        if (isManager) {
            verify(rentalRepository).findById(ID);
        } else {
            verify(rentalRepository).findByIdAndUserId(ID, user.getId());
        }
    }

    @Test
    @DisplayName("Should set actual return date and update inventory")
    void setActualReturnDate_ValidRequest_ReturnsResponse() {
        User user = TestUtil.createUser();
        user.setRole(User.Role.MANAGER);
        Car car = TestUtil.createCar();

        Rental rental = TestUtil.createRental(user, car);
        RentalResponse expected = TestUtil.createRentalResponse(TestUtil.createCarResponse());

        when(rentalRepository.findById(ID)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(expected);

        RentalResponse actual = rentalService.setActualReturnDate(user, ID);

        assertEquals(11, car.getInventory());
        assertEquals(LocalDate.now(), rental.getActualReturnDate());
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should throw an exception if rental "
            + "not found while setting actual return date")
    void setActualReturnDate_InvalidId_ThrowsException() {
        User user = TestUtil.createUser();
        user.setRole(User.Role.MANAGER);
        Car car = TestUtil.createCar();

        when(rentalRepository.findById(INCORRECT_ID)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> rentalService.setActualReturnDate(user, INCORRECT_ID)
        );

        assertNotEquals(11, car.getInventory());
        verifyNoInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Should throw an exception if rental "
            + "is already returned")
    void setActualReturnDateOnReturnedCar_ThrowsException() {
        User user = TestUtil.createUser();
        user.setRole(User.Role.MANAGER);
        Car car = TestUtil.createCar();
        Rental rental = TestUtil.createRental(user, car);
        rental.setActualReturnDate(LocalDate.now());

        when(rentalRepository.findById(ID)).thenReturn(Optional.of(rental));
        assertThrows(CarAlreadyReturnedException.class,
                () -> rentalService.setActualReturnDate(user, ID)
        );

        assertNotEquals(11, car.getInventory());
        verifyNoInteractions(rentalMapper);
    }

    @Test
    @DisplayName("Should process overdue rentals")
    void checkOverdueRentals_Success() {
        Rental overdueRental = TestUtil.createRental(TestUtil.createUser(),
                TestUtil.createCar()
        );

        when(rentalRepository
                .findByReturnDateBeforeAndActualReturnDateIsNull(LocalDate.now()))
                .thenReturn(List.of(overdueRental));

        rentalService.checkOverdueRentals();

        verify(notificationService).sendOverdueMessage(overdueRental);
    }
}
