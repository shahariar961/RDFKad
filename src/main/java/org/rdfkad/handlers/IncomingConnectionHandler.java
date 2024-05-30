package org.rdfkad.handlers;

import org.rdfkad.functions.IDGenerator;
import org.rdfkad.multicast.SensorMulticastSender;
import org.rdfkad.packets.Payload;
import org.rdfkad.packets.SensorDataPayload;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.tables.DataTable;
import org.rdfkad.tables.RoutingTable;
import org.rdfkad.tables.NodeConfig;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IncomingConnectionHandler implements Runnable {
    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final int MULTICAST_PORT = 4446;

    private DataFinder dataFinder = new DataFinder();
    private String ownNodeId;
    private NodeConfig nodeConfig = NodeConfig.getInstance();

    private Socket socket;
    private final int Threshold = 45;
    private ConcurrentHashMap<String, RoutingPacket> routingTable = RoutingTable.getInstance().getMap();
    private ConcurrentHashMap<String, Object> dataTable = DataTable.getInstance().getMap();

    public IncomingConnectionHandler(Socket socket) {
        this.ownNodeId = nodeConfig.getNodeId();
        this.socket = socket;
    }

    @Override
    public void run() {
        handleConnection();
    }

    public void handleConnection() {
        try {
            socket.setSoTimeout(5000);  // Set a timeout of 5 seconds for I/O operations
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

            Object receivedObject = inputStream.readObject();

            if (receivedObject instanceof SensorDataPayload) {
                SensorDataPayload payload = (SensorDataPayload) receivedObject;
                forwardToMulticastGroup(payload);
            } else if (receivedObject instanceof Payload) {
                Payload receivedPayload = (Payload) receivedObject;
                switch (receivedPayload.getRequest()) {
                    case "find":
                        handleFindRequest(receivedPayload, outputStream);
                        break;
                    case "store":
                        handleStoreRequest(receivedPayload, outputStream);
                        break;
                    case "register":
                        handleRegisterRequest(receivedPayload, outputStream);
                        break;
                    case "refresh routing":
                        handleRefreshRoutingRequest(receivedPayload, outputStream);
                        break;
                    case "consensus":
                        handleConsensusRequest(receivedPayload, outputStream);

                        break;
                    case "update routing":
                        updateRoutingTable(receivedPayload);
                    break;
                    default:
                        System.out.println("Unknown request: " + receivedPayload.getRequest());
                }
            }

            inputStream.close();
            outputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleFindRequest(Payload receivedPayload, ObjectOutputStream outputStream) throws IOException {
        String dataId = receivedPayload.getDataId();
        if (dataTable.containsKey(dataId)) {
            Map<String, Object> payloadData = new HashMap<>();
            payloadData.put(dataId, dataTable.get(dataId));
            Payload messagePayload = new Payload("found", receivedPayload.getNodeId(), receivedPayload.getPort(), payloadData);
            outputStream.writeObject(messagePayload);
        } else {
            Payload messagePayload = new Payload("not found", receivedPayload.getNodeId(), receivedPayload.getPort(), dataId);
            outputStream.writeObject(messagePayload);
        }
    }

    private void handleStoreRequest(Payload receivedPayload, ObjectOutputStream outputStream) throws IOException {
        dataTable.put(receivedPayload.getDataId(), receivedPayload.getDataValue());
        System.out.println("Data id " + receivedPayload.getDataId() + " stored successfully");
        Payload messagePayload = new Payload("stored", receivedPayload.getNodeId(), receivedPayload.getPort(), receivedPayload.getDataId());
        outputStream.writeObject(messagePayload);
    }

    private void handleRegisterRequest(Payload receivedPayload, ObjectOutputStream outputStream) throws IOException {
        int multicastId = routingTable.size() + 1;
        String generatedNodeId = IDGenerator.generateNodeId();
        Payload messagePayload = new Payload("registered", generatedNodeId, routingTable, multicastId);
        routingTable.put(generatedNodeId, new RoutingPacket(receivedPayload.getPort(), multicastId));
        outputStream.writeObject(messagePayload);
        System.out.println("Node registered with ID: " + generatedNodeId + " Multicast id: " + multicastId);
    }

    private void handleRefreshRoutingRequest(Payload receivedPayload, ObjectOutputStream outputStream) throws IOException {
        Payload messagePayload = new Payload("refresh routing", receivedPayload.getNodeId(), routingTable);
        outputStream.writeObject(messagePayload);
        System.out.println("Sent routing data to Node: " + receivedPayload.getNodeId());
    }
    private void handleConsensusRequest(Payload receivedPayload, ObjectOutputStream outputStream) throws IOException {

        int objectValue = Integer.parseInt(dataFinder.findCompositeData(receivedPayload.getDataId()));
        if (objectValue > Threshold) {
            Payload messagePayload = new Payload("ok consensus", receivedPayload.getNodeId(), receivedPayload.getPort(), receivedPayload.getDataId());
            outputStream.writeObject(messagePayload);
            System.out.println("Consensus lookup successful");
        } else {
            Payload messagePayload = new Payload("no consensus", receivedPayload.getNodeId(), receivedPayload.getPort(), receivedPayload.getDataId());
            outputStream.writeObject(messagePayload);
            System.out.println("Consensus lookup failed");
        }

    }


    private void forwardToMulticastGroup(SensorDataPayload payload) {
//        try (MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT)) {
//            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
//
//            // Serialize the payload
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
//            objectOutputStream.writeObject(payload);
//            objectOutputStream.flush();
//            byte[] buf = byteArrayOutputStream.toByteArray();
//
//            // Send the packet to the multicast group
//            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, MULTICAST_PORT);
//            multicastSocket.send(packet);
//            System.out.println("Forwarded message to multicast group");
        try {
        SensorMulticastSender.sensorDataMessageSender(payload.getUniqueId(), payload.getDataAddress(), payload.getRequest());
    } catch (NumberFormatException e) {
        System.out.println("Invalid multicast ID. Please provide a numeric value.");
    }
    }
    private void updateRoutingTable(Payload receivedPayload) {
        ConcurrentHashMap<String, RoutingPacket> tempRoutingTable = receivedPayload.getRoutingTable();
        for (Map.Entry<String, RoutingPacket> entry : tempRoutingTable.entrySet()) {
            if (!entry.getKey().equals(ownNodeId)) {
                routingTable.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        System.out.println("Updated routing table");
    }


}
