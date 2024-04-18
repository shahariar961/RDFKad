package org.rdfkad.handlers;

import org.rdfkad.functions.XOR;
import org.rdfkad.packets.Payload;
import org.rdfkad.packets.RDFDataPacket;
import org.rdfkad.packets.RoutingPacket;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class IncomingConnectionHandler {
    private Socket socket;
    private Map<String, RoutingPacket> routingTableMap;
    private static final int BIT_SPACE = 12;

    public IncomingConnectionHandler(Socket socket, Map<String, RoutingPacket> routingTableMap) {
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
                    RoutingPacket packet = new RoutingPacket(port);
                    String newNodeId = generateRandomNodeId();

                    outputStream.writeObject(newNodeId);
                    outputStream.writeObject(routingTableMap);
                    routingTableMap.put(newNodeId, packet);
                } else {
                    outputStream.writeObject(nodeId);
                    outputStream.writeObject(routingTableMap);
                }
            }
            if (receivedObject instanceof String){
                String packet = (String) receivedObject;
                System.out.println(packet);
                RDFDataPacket RDFPacket = new RDFDataPacket(packet);
                System.out.println(RDFPacket.subject + " " + RDFPacket.predicate + " " + RDFPacket.Object);


                //overlayInitiator(RDFPacket);

            }
            if (receivedObject instanceof RDFDataPacket) {
                RDFDataPacket RDFPacket = (RDFDataPacket) receivedObject;
                System.out.println(RDFPacket.subject + " " + RDFPacket.predicate + " " + RDFPacket.Object);

                //overlayInitiator(RDFPacket);
            }
            if (receivedObject instanceof Payload){
                Payload receivedObjectMessage = (Payload) receivedObject;
                System.out.println(receivedObjectMessage.request);
                if (!routingTableMap.containsKey(receivedObjectMessage.nodeId)){
                    System.out.println("Unknown Node Connecting, inserting Node Id in Routing Table");
                    RoutingPacket packet =new RoutingPacket(receivedObjectMessage.port);
                    routingTableMap.put(receivedObjectMessage.nodeId,packet);

                }
//                if(receivedObjectMessage.request.equals("get")) {
//                    System.out.println("Data Get Request");
//                    if(dataTable.containsKey(receivedObjectMessage.dataId)) {
//                        System.out.println("Data Found in Hash Table");
//                        outputStream.writeObject(dataTable.get(receivedObjectMessage.dataId));
//                        System.out.println("Sending Data");
//                        outputStream.flush();
//
//
//                    }else {
//                        outputStream.writeObject("Data not found");
//                    }
//                }
                if (receivedObjectMessage.request.equals("find")) {
                    System.out.println("Sending Routing Data");
                    outputStream.writeObject(routingTableMap);
                }
                if (receivedObjectMessage.request.equals("nearby")){
                    System.out.println("Sending Nearby Node Info");
                    HashMap<String,Integer> xorDistances = new HashMap<String,Integer>();
                    for (Map.Entry<String, RoutingPacket> entry : routingTableMap.entrySet()) {
                        String nodeId = entry.getKey();
                        BigInteger distance = XOR.Distance(receivedObjectMessage.nodeId, nodeId); // Assuming XOR is the class with calculateXORDistance method

                    }

                    // Select the 8 smallest XOR distances

                    HashMap<String, Integer> closestNodes = new HashMap<>();
                    closestNodes = xorDistances.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByValue())
                            .limit(8)
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (e1, e2) -> e1,
                                    HashMap::new
                            ));

                    // Print the closest nodes
                    System.out.println("8 Nodes with Closest XOR Distances:");
                    HashMap<String, Integer> sendingTable = new HashMap<>();
                    for (Map.Entry<String, Integer> entry : closestNodes.entrySet()) {


                        String nodeId = entry.getKey();
                        RoutingPacket packet=routingTableMap.get(nodeId);
                        Integer nodePort= packet.port;
                        sendingTable.put(nodeId,nodePort);


                    }
                    outputStream.writeObject(sendingTable);
                    System.out.println("Nearby Nodes Sent");

                }
            }if (receivedObject instanceof HashMap<?,?>){
                HashMap<String,RoutingPacket> tempTable= (HashMap<String, RoutingPacket>) receivedObject;
                System.out.println("receivedObject routing table");

                for (Map.Entry<String, RoutingPacket> entry : tempTable.entrySet()) {
                    String key = entry.getKey();
                    RoutingPacket value = entry.getValue();

                    // Check if key exists in map2
                    if (!routingTableMap.containsKey(key)) {
                        // Key doesn't exist in map2, insert the entry
                        routingTableMap.put(key, value);
                        System.out.println("New Nodes Added ID:" + key);

                    }
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
