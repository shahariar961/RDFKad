package org.rdfkad.datahandlers;

import org.rdfkad.packets.RoutingPacket;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ConnectionHandler {
    private Socket socket;
    private Map<String, RoutingPacket> routingTableMap;
    private static final int BIT_SPACE = 12;

    public ConnectionHandler(Socket socket, Map<String, RoutingPacket> routingTableMap) {
        this.socket = socket;
        this.routingTableMap = routingTableMap;
    }

    public void handleConnection() {
        try (
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())
        ) {
            Object receivedObject = inputStream.readObject();
            if (receivedObject instanceof String) {
                String nodeInfo = (String) receivedObject;
                String[] nodeId = nodeInfo.split(",");

                if (!routingTableMap.containsKey(nodeId[0])) {
                    Integer port = Integer.parseInt(nodeId[1]);
                    RoutingPacket packet = new RoutingPacket(port, false);
                    String newNodeId = generateRandomNodeId();

                    outputStream.writeObject(newNodeId);
                    outputStream.writeObject(routingTableMap);
                    routingTableMap.put(newNodeId, packet);
                } else {
                    outputStream.writeObject(nodeId);
                    outputStream.writeObject(routingTableMap);
                }
            }

            for (HashMap.Entry<String, RoutingPacket> entry : routingTableMap.entrySet()) {
                String nodeId2 = entry.getKey();
                RoutingPacket info = entry.getValue();

                System.out.println("Node ID: " + nodeId2);
                System.out.println("Port: " + info.port);
                System.out.println("------------------------");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private String generateRandomNodeId() {
        Random random = new Random();
        String nodeId;
        do {
            StringBuilder nodeIdBuilder = new StringBuilder();
            for (int i = 0; i < BIT_SPACE; i++) {
                nodeIdBuilder.append(random.nextInt(2));
            }
            nodeId = nodeIdBuilder.toString();
        } while (routingTableMap.containsKey(nodeId));
        return nodeId;
    }
}
