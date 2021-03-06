package com.kgaurav.balancer.model;

public class Command {
    private int commandCode;
    private InternalItem data;
    private String otherData;

    public int getCommandCode() {
        return commandCode;
    }

    public InternalItem getData() {
        return data;
    }

    public void setCommandCode(int commandCode) {
        this.commandCode = commandCode;
    }

    public void setData(InternalItem data) {
        this.data = data;
    }

    public String getOtherData() {
        return otherData;
    }

    public void setOtherData(String otherData) {
        this.otherData = otherData;
    }
}