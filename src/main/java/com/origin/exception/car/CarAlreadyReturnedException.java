package com.origin.exception.car;

public class CarAlreadyReturnedException extends RuntimeException {
    public CarAlreadyReturnedException(String message) {
        super(message);
    }
}
