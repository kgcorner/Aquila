package com.kgaurav.balancer.model;

/**
 * Created by admin on 3/23/2018.
 */
public class Response {
    private String message;
    private String data;
    private int status;

    public static final int SERVER_STATUS = 3;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
