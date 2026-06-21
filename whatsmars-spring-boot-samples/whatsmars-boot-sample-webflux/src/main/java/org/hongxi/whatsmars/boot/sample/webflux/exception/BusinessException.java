package org.hongxi.whatsmars.boot.sample.webflux.exception;

/**
 * Created by shenhongxi on 2020/8/16.
 */
public class BusinessException extends RuntimeException {

    private int code;
    private String msg;

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "BusinessException(code=" + code + ", msg=" + msg + ")";
    }
}
