package com.petreca.petrecadelivery.courier.management.domain.exception;

public class NoCouriersAvailableException extends RuntimeException {
    public NoCouriersAvailableException(String message) {
        super(message);
    }

    public NoCouriersAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}