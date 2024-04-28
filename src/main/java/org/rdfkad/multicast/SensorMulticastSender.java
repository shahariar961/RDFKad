package org.rdfkad.multicast;

import org.rdfkad.packets.RDFPacket;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.packets.SensorDataPayload;
import org.rdfkad.tables.RoutingTable;

import java.util.concurrent.ConcurrentHashMap;

public class SensorMulticastSender {

    private static ConcurrentHashMap<String, RoutingPacket> routingTable = RoutingTable.getInstance().getMap();



    public static void messageSender() {
        int numberOfUniqueIDs = routingTable.size();
            for (int i = 1; i <= numberOfUniqueIDs; i++) {
                RDFPacket sensorInfo = new RDFPacket("Sensor" + i, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://example.org/Sensor");
                SensorDataPayload payload = new SensorDataPayload(i , sensorInfo);

                SensorMulticastServer.messageSender(payload);  // Use the server class to send the message
            }


    }
}
