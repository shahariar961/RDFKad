package org.rdfkad.handlers;

import org.rdfkad.packets.Payload;
import org.rdfkad.packets.RDFDataPacket;
import org.rdfkad.packets.RoutingPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.rdfkad.packets.Payload;
import org.rdfkad.packets.RDFDataPacket;
import org.rdfkad.packets.RoutingPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class OutgoingConnectionHandler {
    private static String nodeId;
    private static int nodePort;

    public static void connectToNode(String host, RoutingPacket packet, String dataId, String query) {
        try {
            Socket socket = new Socket(host, packet.port);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            Payload messagePayload= new Payload(query,nodeId,nodePort,dataId);
            outputStream.writeObject(messagePayload);
            Object receivedObject = null;
            try {
                receivedObject = inputStream.readObject();
                if (receivedObject instanceof String){
                    System.out.println(receivedObject);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }  catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void connectToNode(String host, RoutingPacket packet,String query) {
        try {
            Socket socket = new Socket(host, packet.port);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            Payload messagePayload= new Payload(query,nodeId,nodePort);
            outputStream.writeObject(messagePayload);
            Object receivedObject = null;
            try {
                receivedObject = inputStream.readObject();
                if (receivedObject instanceof String){
                    System.out.println(receivedObject);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }  catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException  e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendRDF( RoutingPacket RoutingInfo, RDFDataPacket packet) {
        try (Socket socket = new Socket("localhost", RoutingInfo.port);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

            oos.writeObject(packet);
            System.out.println("RDF Packet sent: " + packet);

        } catch (IOException e) {
            System.err.println("Error sending RDF packet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void connectToBootstrapServer(String host, int port, HashMap<String, RoutingPacket> routingTableMap) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
            String nodeInfo = (nodeId + "," + nodePort);
            outputStream.writeObject(nodeInfo);

            Object receivedObject = inputStream.readObject();
            if (receivedObject instanceof String) {
                String receivedNodeId=(String) receivedObject;
                if (nodeId.equals("null")){
                    nodeId=receivedNodeId;
                    System.out.println("Recieved ID: " + nodeId);
                    System.out.println("------------------------");
                }
            }
            Object receivedObject2 = inputStream.readObject();
            if (receivedObject2 instanceof HashMap<?, ?>) {
                HashMap<String, RoutingPacket> recievedHashMap = (HashMap<String, RoutingPacket>) receivedObject2;
                if (!recievedHashMap.isEmpty()){
                    routingTableMap = recievedHashMap;
                }
                for (HashMap.Entry<String, RoutingPacket> entry : recievedHashMap.entrySet()) {
                    String nodeId2 = entry.getKey();
                    RoutingPacket packet = entry.getValue();
                    System.out.println("Node ID: " + nodeId2);
                    System.out.println("Data Port: " + packet.port);
                    System.out.println("------------------------");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}