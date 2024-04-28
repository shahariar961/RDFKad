package org.rdfkad.handlers;

import org.rdfkad.Node;
import org.rdfkad.packets.Payload;
import org.rdfkad.packets.RoutingPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.rdfkad.tables.DataTable;
import org.rdfkad.tables.RoutingTable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class OutgoingConnectionHandler {

    private Node node;
    public OutgoingConnectionHandler(Node node) {
        this.node = node;
    }
    private static String nodeId;
    private static int nodePort = Node.getNodePort();

    private  ConcurrentHashMap <String, RoutingPacket> routingTable= RoutingTable.getInstance().getMap();
    private  ConcurrentHashMap<String, Object> dataTable = DataTable.getInstance().getMap();


    public  String connectToNode(String request, String nodeId, String dataId) throws IOException {
        RoutingPacket routingPacket = routingTable.get(nodeId);
        if (routingPacket == null) {
            throw new IOException("No routing information available for nodeId: " + nodeId);
        }
        try {

            Socket socket = new Socket(routingPacket.getHost(), RoutingPacket.getPort());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            Payload messagePayload= new Payload(request,nodeId,nodePort,dataId);
            outputStream.writeObject(messagePayload);
            Object receivedObject = null;
            try {
                receivedObject = inputStream.readObject();
                Payload receivedPayload = (Payload) receivedObject;
                if (receivedPayload.getRequest().equals("found")) {
                    dataTable.put(dataId, receivedPayload.getData());
                    System.out.println(receivedObject);
                    return "Data found";
                }else if (receivedPayload.getRequest().equals("not found")) {

                    return null;
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }  catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public void connectToBootstrapServer(String request,String host, int port) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
            Payload messagePayload= new Payload(request,Node.getNodeId(),Node.getNodePort());
            outputStream.writeObject(messagePayload);

            Object receivedObject = inputStream.readObject();
            if (receivedObject instanceof Payload) {
                Payload receivedPayload =(Payload) receivedObject;
                if (receivedPayload.getRequest().equals("registered")) {
                    nodeId= receivedPayload.getNodeId();
                    node.setNodeId(nodeId);
                    node.setMulticastId(receivedPayload.getMulticastId());
                    System.out.println("Register to Network with ID :"+receivedPayload.getNodeId() +"Multicast id :" + receivedPayload.getMulticastId());
                    ConcurrentHashMap<String, RoutingPacket> tempRoutingTable = receivedPayload.getRoutingTable();
                    for (Map.Entry<String, RoutingPacket> entry : tempRoutingTable.entrySet()) {
                        if (!entry.getKey().equals(nodeId)) {
                            routingTable.putIfAbsent(entry.getKey(), entry.getValue());
                        }
                    }


                }
                if (receivedPayload.getRequest().equals("routing info")) {
                    ConcurrentHashMap<String, RoutingPacket> tempRoutingTable = receivedPayload.getRoutingTable();
                    for (Map.Entry<String, RoutingPacket> entry : tempRoutingTable.entrySet()) {
                        if (!entry.getKey().equals(nodeId)) {
                            routingTable.putIfAbsent(entry.getKey(), entry.getValue());
                        }
                    }


                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}