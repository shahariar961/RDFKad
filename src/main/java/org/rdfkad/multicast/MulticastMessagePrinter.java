package org.rdfkad.multicast;

import org.rdfkad.packets.SensorDataPayload;
import org.rdfkad.BootstrapServer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

public class MulticastMessagePrinter implements Runnable {
    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final int MULTICAST_PORT = 4446;
    private static List<Long> latencies = new ArrayList<>();

    @Override
    public void run() {
        try (MulticastSocket socket = new MulticastSocket(MULTICAST_PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            socket.joinGroup(group);

            while (!Thread.currentThread().isInterrupted()) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);

                // Record the receive timestamp
                long receiveTimestamp = System.currentTimeMillis();

                // Deserialize the received data
                try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData());
                     ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

                    SensorDataPayload receivedPayload = (SensorDataPayload) objectInputStream.readObject();
                    System.out.println("Received message from Node: " + receivedPayload.getUniqueId() + " Alarm type " + receivedPayload.getRequest());

                    // Calculate the latency using the stored send timestamp
                    int multicastId = receivedPayload.getUniqueId();
                    long sendTimestamp = BootstrapServer.getSendTimestamp(multicastId);
                    if (sendTimestamp != -1) {
                        long latency = receiveTimestamp - sendTimestamp;
                        latencies.add(latency);
                        System.out.println("Latency: " + latency + " ms");
                    } else {
                        System.out.println("Send timestamp not found for multicast ID: " + multicastId);
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Error during deserialization: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error in MulticastMessagePrinter: " + e.getMessage());
        }
    }

    public static List<Long> getLatencies() {
        return latencies;
    }
}
