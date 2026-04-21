package com.origin.mapper;

import com.origin.config.MapperConfig;
import com.origin.dto.rental.CreateRentalRequest;
import com.origin.dto.rental.RentalResponse;
import com.origin.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "car", target = "car")
    RentalResponse toDto(Rental rental);

    Rental toModel(CreateRentalRequest createRentalRequest);
}
