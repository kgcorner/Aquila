package com.kgaurav.balancer.model;

public class NodeInfo {
    private String internalId;
    private Node node;

    public NodeInfo(String internalId, Node node) {
        this.internalId = internalId;
        this.node = node;
    }

    public String getInternalId() {
        return internalId;
    }

    public Node getNode() {
        return node;
    }
}
