package org.rdfkad.packets;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Payload implements Serializable {
    private String request;  // Type of request: 'connect', 'find', 'found', etc.
    private String nodeId;   // Node ID, used in various contexts
    private Integer port;    // Port number for connecting
    private Map<String, Object> data;  // Generic data container
    private Integer multicastId;  // Multicast ID for 'connect' requests

    private String message;



    private Object dataValue;



    private ConcurrentHashMap<String, RoutingPacket> routingTable;  // Routing table for 'connect' requests
    private String dataId;   // Specific data ID for 'find' requests

    // Constructor for 'connect' request
    public Payload(String request, String nodeId, int port, ConcurrentHashMap<String, RoutingPacket> routingTable, int multicastId) {
        this.request = request;
        this.nodeId = nodeId;
        this.port = port;
        this.routingTable = routingTable;
        this.multicastId=multicastId;// Storing routing table as data
    }
    public Payload(String request, String nodeId, ConcurrentHashMap<String, RoutingPacket> routingTable, int multicastId) {
        this.request = request;
        this.nodeId = nodeId;
        this.routingTable = routingTable;
        this.multicastId=multicastId;// Storing routing table as data
    }
    public Payload(String request, String nodeId, int port) {
        this.request = request;
        this.nodeId = nodeId;
        this.port = port;
    }
    public Payload(String request,ConcurrentHashMap<String, RoutingPacket> routingTable ){
        this.request = request;
        this.routingTable = routingTable;
    }
    public Payload(String nodeId, String dataId) {
        this.dataId = dataId;
        this.nodeId = nodeId;
    }
    public Payload(String request, String nodeId,  ConcurrentHashMap<String, RoutingPacket> routingTable) {
        this.request = request;
        this.nodeId = nodeId;

        this.routingTable = routingTable;  // Storing routing table as data
    }
    public Payload(String request, String nodeId, int port, int multicastId) {
        this.request = request;
        this.nodeId = nodeId;
        this.port = port;
        this.multicastId = multicastId;
    }

    public Payload(String request, String nodeId, int port, Map<String, Object> data) {
        this.request = request;
        this.nodeId = nodeId;
        this.port = port;
        this.data =   data;  // Storing routing table as data
    }

    // Constructor for 'find' request
    public Payload(String request, String nodeId, int port ,  String dataId) {
        this.request = request;
        this.dataId = dataId;
        this.nodeId = nodeId;
        this.port = port;
    }
    public Payload(String request, String nodeId, int port ,  String dataId, Object dataValue) {
        this.request = request;
        this.dataId = dataId;
        this.nodeId = nodeId;
        this.port = port;
        this.dataValue = dataValue;
    }

    public Payload(int multicastId, String message, String request){
        this.multicastId = multicastId;
        this.message = message;
        this.request = request;
    }

    // Constructor for 'found' request
    public Payload(String request, HashMap<String, Object> data) {
        this.request = request;
        this.data = data;
    }

    // Getters and setters as needed for serialization and access in the network
    public String getRequest() {
        return request;
    }

    public String getNodeId() {
        return nodeId;
    }

    public Integer getPort() {
        return port;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getDataId() {
        return dataId;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public  ConcurrentHashMap<String, RoutingPacket> getRoutingTable() {
        return routingTable;
    }

    public void setRoutingTable(ConcurrentHashMap<String, RoutingPacket> routingTable) {
        this.routingTable = routingTable;
    }

    public Integer getMulticastId() {
        return multicastId;
    }

    public void setMulticastId(Integer multicastId) {
        this.multicastId = multicastId;
    }
    public Object getDataValue() {
        return dataValue;
    }

    public void setDataValue(Object dataValue) {
        this.dataValue = dataValue;
    }

    public String getMessage() {
        return message;
    }
}
