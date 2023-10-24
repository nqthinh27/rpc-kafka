package com.provision.rpckafka.exception;

public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException(String errorMessage) {
        super(errorMessage);
    }
}
