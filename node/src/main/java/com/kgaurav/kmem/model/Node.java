package com.kgaurav.kmem.model;

/**
 * Created by admin on 3/23/2018.
 */
public class Node {
    private String address;
    private int port;
    private int type;
    private int status;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if(obj instanceof Node) {
            Node node =  (Node) obj;
            result = node.address.equals(this.address) && node.port == this.port;
        }
        return result;
    }
}
