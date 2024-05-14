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
import org.rdfkad.tables.NodeConfig;
import org.rdfkad.tables.RoutingTable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class OutgoingConnectionHandler {

    private  String ownNodeId ;
    private  NodeConfig nodeConfig = NodeConfig.getInstance();
    private  int nodePort;


    public OutgoingConnectionHandler( ) {
    this.ownNodeId = nodeConfig.getNodeId();
    this.nodePort = nodeConfig.getNodePort();
    }


    private  ConcurrentHashMap <String, RoutingPacket> routingTable= RoutingTable.getInstance().getMap();
    private  ConcurrentHashMap<String, Object> dataTable = DataTable.getInstance().getMap();


    public  String connectToNode(String request, RoutingPacket routingPacket, String dataId) throws IOException {
        if (routingPacket == null) {
            throw new IOException("No routing information available for nodeId: " );
        }
        try {

            Socket socket = new Socket(routingPacket.getHost(), routingPacket.getPort());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            if (request.equals("store")) {
            Object dataValue = dataTable.get(dataId);
            Payload messagePayload= new Payload(request,ownNodeId,nodePort,dataId,dataValue);
                outputStream.writeObject(messagePayload);
            }
             if (request.equals("find") ) {
            Payload messagePayload= new Payload(request,ownNodeId,nodePort,dataId);
                 outputStream.writeObject(messagePayload);
            }

            Object receivedObject = null;
            try {
                receivedObject = inputStream.readObject();
                Payload receivedPayload = (Payload) receivedObject;
                if (receivedPayload.getRequest().equals("stored")) {
                    System.out.println("Stored Successfully Data Id "+receivedPayload.getDataId() + "at Node :"  +receivedPayload.getNodeId());

                } else  if (receivedPayload.getRequest().equals("found")) {
                    dataTable.put(dataId, receivedPayload.getData());
                    System.out.println("Data found" + receivedObject);
                }else if (receivedPayload.getRequest().equals("not found")) {

                    System.out.println("Data not found");
                }else if (request.equals("modified") ) {
                    System.out.println("Data Modified");
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public void connectToBootstrapServer(String request,String host, int port) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
            Payload messagePayload= new Payload(request,ownNodeId,nodePort);
            outputStream.writeObject(messagePayload);

            Object receivedObject = inputStream.readObject();
            if (receivedObject instanceof Payload) {
                Payload receivedPayload =(Payload) receivedObject;
                if (receivedPayload.getRequest().equals("registered")) {
                    nodeConfig.setNodeId( receivedPayload.getNodeId());
                    nodeConfig.setMulticastId(receivedPayload.getMulticastId());
                    System.out.println("Register to Network with ID :"+receivedPayload.getNodeId() +"Multicast id :" + receivedPayload.getMulticastId());
                    ConcurrentHashMap<String, RoutingPacket> tempRoutingTable = receivedPayload.getRoutingTable();
                    for (Map.Entry<String, RoutingPacket> entry : tempRoutingTable.entrySet()) {
                        if (!entry.getKey().equals(ownNodeId)) {
                            routingTable.putIfAbsent(entry.getKey(), entry.getValue());
                        }
                    }


                }
                if (receivedPayload.getRequest().equals("refresh routing")) {
                    ConcurrentHashMap<String, RoutingPacket> tempRoutingTable = receivedPayload.getRoutingTable();
                    for (Map.Entry<String, RoutingPacket> entry : tempRoutingTable.entrySet()) {
                        if (!entry.getKey().equals(ownNodeId)) {
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