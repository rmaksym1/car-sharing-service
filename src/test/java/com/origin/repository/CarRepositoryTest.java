package com.origin.repository;

import com.origin.model.Car;
import com.origin.repository.car.CarRepository;
import com.origin.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import static com.origin.util.TestConstants.CLEANUP_DB_PATH;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = CLEANUP_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CarRepositoryTest {

    @Autowired
    private CarRepository carRepository;

    @Test
    @DisplayName("Should save car and return it by id")
    void shouldSaveCarAndReturnItById() {
        Car car = TestUtil.createCar();

        Car saved = carRepository.save(car);
        Car actual = carRepository.findById(saved.getId()).orElseThrow();

        assertNotNull(saved.getId());
        assertEquals(saved.getBrand(), actual.getBrand());
        assertEquals(saved.getModel(), actual.getModel());
    }

    @Test
    @DisplayName("Should soft delete car")
    void shouldSoftDeleteCar() {
        Car car = TestUtil.createCar();
        Car saved = carRepository.save(car);

        carRepository.deleteById(saved.getId());

        assertTrue(carRepository.findById(saved.getId()).isEmpty(),
                "Car must be soft deleted!");
    }

    @Test
    @DisplayName("Should throw exception when saving car with null fields")
    void shouldThrowException_WhenSavingInvalidCar() {
        Car car = new Car();

        assertThrows(Exception.class, () ->
            carRepository.save(car)
        );
    }
}
