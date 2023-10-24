package vn.com.rpckafka.exception;

public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException(String errorMessage) {
        super(errorMessage);
    }
}
