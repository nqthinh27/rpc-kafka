package vn.com.rpckafka.exception;

public class ForbiddenException extends RuntimeException{
    public ForbiddenException(String errorMessage) {
        super(errorMessage);
    }
}
