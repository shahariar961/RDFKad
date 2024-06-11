package org.rdfkad.handlers;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RDFPacketSender {

    /**
     * Sends an RDFDataPacket to the specified host and port.
     *
     * @param host   The host to connect to.
     * @param port   The port to connect on.
     * @param packet The RDFDataPacket to send.
     */
    public static void sendRDFPacket(String host, int port, String packet) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

//            String serializedPacket = packet.serialize();
            oos.writeObject(packet);
           // System.out.println("RDF Packet sent: " + packet);

        } catch (IOException e) {
            System.err.println("Error sending RDF packet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Example usage
        String packet = "subject1|predicate1|object1";
        sendRDFPacket("localhost", 8082, packet); // Replace "localhost" and 12345 with actual target host and port
    }
}