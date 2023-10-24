package com.provision.rpckafka.model;

public class ErrorRes {
    private int code;
    private String message;

    private String detailErrorMessage;


    public ErrorRes(int code, String message, String detailErrorMessage) {
        this.code = code;
        this.message = message;
        this.detailErrorMessage = detailErrorMessage;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ErrorRes(String detailErrorMessage) {
        this.detailErrorMessage = detailErrorMessage;
    }

    public String getDetailErrorMessage() {
        return detailErrorMessage;
    }

    public void setDetailErrorMessage(String detailErrorMessage) {
        this.detailErrorMessage = detailErrorMessage;
    }
}
