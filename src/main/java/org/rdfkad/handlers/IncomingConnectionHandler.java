package org.rdfkad.handlers;

import org.rdfkad.functions.IDGenerator;
import org.rdfkad.packets.Payload;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.tables.DataTable;
import org.rdfkad.tables.RoutingTable;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class IncomingConnectionHandler {
    private Socket socket;
    private ConcurrentHashMap<String, RoutingPacket> routingTable = RoutingTable.getInstance().getMap();
    private ConcurrentHashMap<String, Object> dataTable = DataTable.getInstance().getMap();

    public IncomingConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    public void handleConnection() {
        try (
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())
        ) {
            Object receivedObject = inputStream.readObject();

            if (receivedObject instanceof Payload){
                Payload receivedPayload = (Payload) receivedObject;
                if (receivedPayload.getRequest().equals("find")) {
                    if (dataTable == null) {
                        Payload messagePayload = new Payload("not found", receivedPayload.getNodeId(), receivedPayload.getPort(), receivedPayload.getDataId());
                        outputStream.writeObject(messagePayload);
                    } else {
                        String dataId = receivedPayload.getDataId();
                        if (dataTable.containsKey(dataId)) {
                            Map<String,Object>payloadData = new HashMap<>();
                            payloadData.put(receivedPayload.getDataId(), dataTable.get(dataId));
                            Payload messagePayload = new Payload("found", receivedPayload.getNodeId(), receivedPayload.getPort(), payloadData);
                            outputStream.writeObject(messagePayload);
                        } else {
                            Payload messagePayload = new Payload("not found", receivedPayload.getNodeId(), receivedPayload.getPort(), receivedPayload.getDataId());
                            outputStream.writeObject(messagePayload);
                        }

                    }

                } else if (receivedPayload.getRequest().equals("store")) {
                    if (routingTable.containsKey(receivedPayload.getDataId())) {
                        dataTable.put(receivedPayload.getDataId(),receivedPayload.getDataValue());
                        System.out.println("Data id "+ receivedPayload.getDataId()+"stored successfully");
                        Payload messagePayload = new Payload("modfied", receivedPayload.getNodeId(), receivedPayload.getPort(), receivedPayload.getDataId());
                        outputStream.writeObject(messagePayload);

                        // Handle the case where the data ID exists in the routing table
                        // You can add your specific logic here if needed
                    } else {
                        dataTable.put(receivedPayload.getDataId(), receivedPayload.getDataValue());
                        System.out.println("Data id "+ receivedPayload.getDataId()+"stored successfully");
                        Payload messagePayload = new Payload("stored", receivedPayload.getNodeId(), receivedPayload.getPort(), receivedPayload.getDataId());
                        outputStream.writeObject(messagePayload);
                    }
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public void handleConnectionBootstrap() {
        try (
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())
        ) {
            Object receivedObject = inputStream.readObject();
            if (receivedObject instanceof Payload){
                Payload receivedPayload = (Payload) receivedObject;
                if (receivedPayload.getRequest().equals("register")) {
                    int multicastId;
                    if (routingTable.isEmpty()) {
                         multicastId = 1;}
                    else {
                         multicastId = routingTable.size()+1;
                    }
                    String  generatedNodeId = IDGenerator.generateNodeId();
                    Payload messagePayload = new Payload("registered", generatedNodeId, routingTable, multicastId);
                    routingTable.put(generatedNodeId,new RoutingPacket(receivedPayload.getPort()));
                    outputStream.writeObject(messagePayload);
                    System.out.println("Node registered with ID: " + generatedNodeId +"Multicast id :" + multicastId);

                }
                if (receivedPayload.getRequest().equals("refresh routing")) {
                    Payload messagePayload = new Payload("refresh routing", receivedPayload.getNodeId(), routingTable);
                    outputStream.writeObject(messagePayload);
                    System.out.println("Sent routing data to Node:" + receivedPayload.getNodeId());

                }

                }



    } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }}
