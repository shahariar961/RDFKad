package org.rdfkad.tables;

public class NodeConfig {
    private static NodeConfig instance;

    // Mutable node ID, initialized to null
    private String nodeId = "null";
    private Integer multicastId = 0;
    private  Integer nodePort = 0;

    // Private constructor to prevent instantiation
    private NodeConfig() {}

    // Method to get the single instance
    public static synchronized NodeConfig getInstance() {
        if (instance == null) {
            instance = new NodeConfig();
        }
        return instance;
    }

    // Setter for node ID, allowing it to be set only once
    public synchronized void setNodeId(String nodeId) {
        if (this.nodeId.equals("null")) {
            this.nodeId = nodeId;
        } else {
            System.out.println("Node ID already set");
        }
    }

    // Getter for node ID
    public synchronized String getNodeId() {

        return nodeId;
    }


    public synchronized Integer getMulticastId() {

        return multicastId;
    }
    public synchronized void  setMulticastId(int multicastId) {
        if (this.multicastId == 0) {
            this.multicastId = multicastId;
        } else {
            System.out.println("Multicast id already set");
        }

    }
    public synchronized void  setNodePort(int nodePort) {
        if (this.nodePort == 0) {
            this.nodePort = nodePort;
        } else {
            System.out.println("Node Port  id already set");
        }

    }
    public synchronized Integer getNodePort() {

        return nodePort;
    }
}
