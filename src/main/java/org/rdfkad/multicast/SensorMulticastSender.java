package org.rdfkad.multicast;

import org.rdfkad.packets.SensorDataPayload;
import org.rdfkad.packets.RDFPacket;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.tables.RoutingTable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.ConcurrentHashMap;

public class SensorMulticastSender {
    private static final String MULTICAST_GROUP = System.getenv("MULTICAST_GROUP") != null ? System.getenv("MULTICAST_GROUP") : "230.0.0.1";
    private static final int PORT = Integer.parseInt(System.getenv("MULTICAST_PORT") != null ? System.getenv("MULTICAST_PORT") : "4446");

    private static ConcurrentHashMap<String, RoutingPacket> routingTable = RoutingTable.getInstance().getMap();

    public static void sensorDataMessageSender(int multicastId, int message, String request) {
        RDFPacket sensorInfo = new RDFPacket("Sensor" + multicastId, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", String.valueOf(message));
        SensorDataPayload payload = new SensorDataPayload(multicastId, sensorInfo, request);

        sendMulticastMessage(payload);
    }

    public static void sensorDataMessageSender(int multicastId, String dataAddress, String request) {
        SensorDataPayload payload = new SensorDataPayload(multicastId, dataAddress, request);

        sendMulticastMessage(payload);
    }

    public static void messageSender(int multicastId, String message, String request) {
        SensorDataPayload payload = new SensorDataPayload(multicastId, message, request);

        sendMulticastMessage(payload);
    }

    private static void sendMulticastMessage(SensorDataPayload payload) {
        // Serialize the SensorDataPayload object
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeObject(payload);
            objectOutputStream.flush();
            byte[] buf = byteArrayOutputStream.toByteArray();

            // Create a MulticastSocket and send the DatagramPacket
            try (MulticastSocket sendSocket = new MulticastSocket()) {
                InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
                sendSocket.send(packet);
            } catch (IOException e) {
                System.out.println("Error sending multicast message: " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("Error during serialization: " + e.getMessage());
        }
    }
}
