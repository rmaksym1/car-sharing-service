package com.origin.mapper;

import com.origin.config.MapperConfig;
import com.origin.dto.payment.CreatePaymentRequest;
import com.origin.dto.payment.PaymentResponse;
import com.origin.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    @Mapping(source = "rental.id", target = "rentalId")
    PaymentResponse toDto(Payment payment);

    @Mapping(source = "rentalId", target = "rental.id")
    @Mapping(source = "paymentType", target = "type")
    Payment toModel(CreatePaymentRequest request);
}
