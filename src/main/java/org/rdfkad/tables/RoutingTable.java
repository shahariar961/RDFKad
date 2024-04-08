package org.rdfkad.tables;

import java.io.Serializable;

public class RoutingTable implements Serializable {
    private String nodeId;
    private int port;

    public RoutingTable(String nodeId, int port) {
        this.nodeId = nodeId;
        this.port = port;
    }

    public String getNodeId() {
        return nodeId;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "RoutingTable{" +
                "nodeId='" + nodeId + '\'' +
                ", port=" + port +
                '}';
    }
}