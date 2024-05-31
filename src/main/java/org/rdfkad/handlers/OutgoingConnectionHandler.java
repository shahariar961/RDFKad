package org.rdfkad.handlers;

import org.rdfkad.packets.Payload;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.tables.DataTable;
import org.rdfkad.tables.NodeConfig;
import org.rdfkad.tables.RoutingTable;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class OutgoingConnectionHandler {
    private String ownNodeId;
    private NodeConfig nodeConfig = NodeConfig.getInstance();
    private int nodePort;

    public OutgoingConnectionHandler() {
        this.ownNodeId = nodeConfig.getNodeId();
        this.nodePort = nodeConfig.getNodePort();
    }

    private ConcurrentHashMap<String, RoutingPacket> routingTable = RoutingTable.getInstance().getMap();
    private ConcurrentHashMap<String, Object> dataTable = DataTable.getInstance().getMap();

    public String connectToNode(String request, RoutingPacket routingPacket, String dataId, CountDownLatch latch) throws IOException {
        if (routingPacket == null) {
            throw new IOException("No routing information available for nodeId: ");
        }
        try (Socket socket = new Socket(routingPacket.getAddress(), routingPacket.getPort());
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

            Payload messagePayload;
            if (request.equals("consensus")) {
                messagePayload = new Payload(request, ownNodeId, nodePort, dataId);
                outputStream.writeObject(messagePayload);
            }

            Object receivedObject = inputStream.readObject();
            if (receivedObject instanceof Payload) {
                Payload receivedPayload = (Payload) receivedObject;
                handleReceivedPayload(receivedPayload, dataId);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String connectToNode(String request, RoutingPacket routingPacket, String dataId) throws IOException {
        if (routingPacket == null) {
            throw new IOException("No routing information available for nodeId: ");
        }
        try (Socket socket = new Socket(routingPacket.getAddress(), routingPacket.getPort());
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

            Payload messagePayload;
            if (request.equals("store")) {
                Object dataValue = dataTable.get(dataId);
                messagePayload = new Payload(request, ownNodeId, nodePort, dataId, dataValue);
                outputStream.writeObject(messagePayload);
            } else if (request.equals("find")) {
                messagePayload = new Payload(request, ownNodeId, nodePort, dataId);
                outputStream.writeObject(messagePayload);
            } else if (request.equals("consensus")) {
                messagePayload = new Payload(request, ownNodeId, nodePort, dataId);
                outputStream.writeObject(messagePayload);
            }

            Object receivedObject = inputStream.readObject();
            if (receivedObject instanceof Payload) {
                Payload receivedPayload = (Payload) receivedObject;
                handleReceivedPayload(receivedPayload, dataId);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private boolean handleReceivedPayload(Payload receivedPayload, String dataId) {
        switch (receivedPayload.getRequest()) {
            case "stored":
                System.out.println("Stored Successfully Data Id " + receivedPayload.getDataId() + " at Node: " + receivedPayload.getNodeId());
                break;
            case "found":
                Map<String, Object> data = receivedPayload.getData();
                Object dataValue = data.get(dataId);
                dataTable.put(dataId, dataValue);
                System.out.println("Data found: " + dataValue);
                return true;

            case "not found":
                System.out.println("Data not found");
                return false;

            case "ok consensus":
                System.out.println("Consensus reached for Data Id " + receivedPayload.getDataId() + " at Node: " + receivedPayload.getNodeId());
                return true;

            case "no consensus":
                System.out.println("Consensus failed");
                return false;
            default:
                System.out.println("Unknown response: " + receivedPayload.getRequest());

        }
        return false;
    }

    public Boolean connectConsensus(String request, RoutingPacket routingPacket, String dataId) throws IOException {
        if (routingPacket == null) {
            throw new IOException("No routing information available for nodeId: ");
        }
        try (Socket socket = new Socket(routingPacket.getAddress(), routingPacket.getPort());
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

            Payload messagePayload;
            if (request.equals("consensus")) {
                messagePayload = new Payload(request, ownNodeId, nodePort, dataId);
                outputStream.writeObject(messagePayload);
            }
            Object receivedObject = inputStream.readObject();
            if (receivedObject instanceof Payload) {
                Payload receivedPayload = (Payload) receivedObject;
                if (handleReceivedPayload(receivedPayload, dataId)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public void connectToBootstrapServer(String request, String host, int port) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
            if (request.equals("update routing")) {
                refreshRoutingBootstrapServer(request, outputStream);
            } else {
                Payload messagePayload = new Payload(request, ownNodeId, nodePort);
                outputStream.writeObject(messagePayload);

                Object receivedObject = inputStream.readObject();
                if (receivedObject instanceof Payload) {
                    Payload receivedPayload = (Payload) receivedObject;
                    if (receivedPayload.getRequest().equals("registered")) {
                        handleRegistration(receivedPayload);
                    } else if (receivedPayload.getRequest().equals("refresh routing")) {
                        updateRoutingTable(receivedPayload);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void refreshRoutingBootstrapServer(String request, ObjectOutputStream outputStream) throws IOException {
        routingTable = RoutingTable.getInstance().getMap();
        Payload messagePayload = new Payload("update routing", routingTable);
        outputStream.writeObject(messagePayload);
    }

    private void handleRegistration(Payload receivedPayload) {
        nodeConfig.setNodeId(receivedPayload.getNodeId());
        nodeConfig.setMulticastId(receivedPayload.getMulticastId());
        System.out.println("Registered to Network with ID: " + receivedPayload.getNodeId() + " Multicast id: " + receivedPayload.getMulticastId());
        updateRoutingTable(receivedPayload);
    }

    private void updateRoutingTable(Payload receivedPayload) {
        ConcurrentHashMap<String, RoutingPacket> tempRoutingTable = receivedPayload.getRoutingTable();
        for (Map.Entry<String, RoutingPacket> entry : tempRoutingTable.entrySet()) {
            if (!entry.getKey().equals(ownNodeId)) {
                routingTable.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
    }
}
