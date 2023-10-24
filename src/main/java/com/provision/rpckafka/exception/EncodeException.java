package com.provision.rpckafka.exception;

import org.zalando.problem.Status;

public class EncodeException extends AbstractThrowableException {
    public EncodeException(String message) {
        super(null, message, Status.INTERNAL_SERVER_ERROR);
    }

    public EncodeException(String message, Status status) {
        super(null, message, status);
    }
}
