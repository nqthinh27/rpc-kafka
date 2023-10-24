package vn.com.rpckafka.exception;

public class CustomException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int errorCode;
    private String errorMessage;
    private String detailErrorMessage;


    public CustomException(int errorCode, String errorMessage) {
        this(errorCode, errorMessage, null);
    }

    public CustomException(int errorCode, String errorMessage, String detailErrorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.detailErrorMessage = detailErrorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getDetailErrorMessage() {
        return detailErrorMessage;
    }

    public void setDetailErrorMessage(String detailErrorMessage) {
        this.detailErrorMessage = detailErrorMessage;
    }
}
