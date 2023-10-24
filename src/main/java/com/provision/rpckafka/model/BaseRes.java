package com.provision.rpckafka.model;


import com.provision.rpckafka.constant.ResponseCode;

public class BaseRes<T> {
    private int code;
    private String message;
    private T detail;

    public BaseRes() {}

    public BaseRes(int code, String message, T detail) {
        this.code = code;
        this.message = message;
        this.detail = detail;
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

    public T getDetail() {
        return detail;
    }

    public void setDetail(T detail) {
        this.detail = detail;
    }

    public static BaseRes buildSuccessfulRes(){
        return new BaseRes(ResponseCode.CODE.TRANSACTION_SUCCESSFUL, ResponseCode.MSG.TRANSACTION_SUCCESSFUL, null);
    }

    public static BaseRes buildSuccessfulRes(Object detail){
        return new BaseRes(ResponseCode.CODE.TRANSACTION_SUCCESSFUL, ResponseCode.MSG.TRANSACTION_SUCCESSFUL, detail);
    }

    public static BaseRes buildSuccessfulRes(int code, String message, Object detail){
        return new BaseRes(code, message, detail);
    }
}
