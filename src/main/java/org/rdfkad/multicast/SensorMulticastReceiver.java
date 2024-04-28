package org.rdfkad.multicast;

import org.rdfkad.Node;
import org.rdfkad.packets.RDFPacket;
import org.rdfkad.packets.SensorDataPayload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

public class SensorMulticastReceiver  implements  Runnable{

    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final int PORT = 4446;
// The unique ID this client responds to

    public  void run() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            socket.joinGroup(group);

            while (true) {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                // Deserialize the incoming data
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData());
                try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
                    Object object = objectInputStream.readObject();
                    if (object instanceof SensorDataPayload) {
                        SensorDataPayload payload = (SensorDataPayload) object;
                        int uniqueId = payload.getUniqueId();
                        int multicastId = Node.getMulticastId();
                        // Check if the unique ID matches
                        if (multicastId == uniqueId) {
                            System.out.println("Received relevant payload:");
                            System.out.println("Unique ID: " + payload.getUniqueId());
                            RDFPacket rdfData = payload.getRdfData();
                            System.out.println("RDF Data: Subject - " + rdfData.getSubject() +
                                    ", Predicate - " + rdfData.getPredicate() +
                                    ", Object - " + rdfData.getObject());
                        } else {
                            System.out.println("Received payload for another sensor: " + payload.getUniqueId());
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Error during deserialization: " + e.getMessage());
                }
            }
        } catch (SocketException e) {
            System.out.println("Socket Exception: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Exception: " + e.getMessage());
        }

    }
}
