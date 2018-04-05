package com.kgaurav.balancer.model;

/**
 * Created by admin on 4/5/2018.
 */

/**
 * Represents a node
 */
public class Node {
    private String address;
    private int port;
    private Node_Type type;
    private boolean active;
    public enum Node_Type {
        MAIN,BACKUP
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Node_Type getType() {
        return type;
    }

    public void setType(Node_Type type) {
        this.type = type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
