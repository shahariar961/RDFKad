package org.rdfkad.multicast;

import org.rdfkad.packets.SensorDataPayload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SensorMulticastServer implements Runnable {
    private static final int PORT = 4446;
    private static final String MULTICAST_GROUP = "230.0.0.1";
    private MulticastSocket socket;

    // Constructor to set up the socket
    public SensorMulticastServer() throws IOException {
        this.socket = new MulticastSocket(PORT);
        InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
        socket.joinGroup(group); // Joining the multicast group to listen
    }

    @Override
    public void run() {
        byte[] buf = new byte[1024];
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                // Handle the received data here
                // System.out.println("Received data: " + new String(packet.getData(), 0, packet.getLength()));
            } catch (IOException e) {
                System.out.println("IOException in listener: " + e.getMessage());
                break;  // Exit if the socket encounters an error
            }
        }
        try {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            socket.leaveGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
    }

    // Static method to send messages
    public static void messageSender(SensorDataPayload payload) {
        try (MulticastSocket sendSocket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);

            // Serialize the SensorDataPayload object
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(payload);
            objectOutputStream.flush();
            byte[] buf = byteArrayOutputStream.toByteArray();

            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
            sendSocket.send(packet);
            System.out.println("Sent SensorDataPayload object with RDF data");
        } catch (IOException e) {
            System.out.println("Error sending multicast message: " + e.getMessage());
        }
    }
}
