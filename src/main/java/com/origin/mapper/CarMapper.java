package com.origin.mapper;

import com.origin.config.MapperConfig;
import com.origin.dto.car.CarResponse;
import com.origin.dto.car.CreateCarRequest;
import com.origin.dto.car.UpdateCarRequest;
import com.origin.model.Car;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    CarResponse toDto(Car car);

    @Mapping(source = "carType", target = "carType")
    Car toModel(CreateCarRequest carRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCar(UpdateCarRequest carRequest, @MappingTarget Car car);
}
