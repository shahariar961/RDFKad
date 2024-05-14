package org.rdfkad.multicast;

import org.rdfkad.packets.RDFPacket;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.packets.SensorDataPayload;
import org.rdfkad.tables.RoutingTable;

import java.util.concurrent.ConcurrentHashMap;

public class SensorMulticastSender {

    private static ConcurrentHashMap<String, RoutingPacket> routingTable = RoutingTable.getInstance().getMap();



//    public static void messageSender() {
//        int numberOfUniqueIDs = routingTable.size();
//            for (int i = 1; i <= numberOfUniqueIDs; i++) {
//                RDFPacket sensorInfo = new RDFPacket("Sensor" + i, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://example.org/Sensor/25");
//                SensorDataPayload payload = new SensorDataPayload(i , sensorInfo, request);
//
//                SensorMulticastServer.messageSender(payload);  // Use the server class to send the message
//            }
//
//
//    }
    public static void singularMessageSender(int multicastid, String temperature, String request) {


            RDFPacket sensorInfo = new RDFPacket("Sensor" + multicastid, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", String.valueOf(temperature));
            SensorDataPayload payload = new SensorDataPayload(multicastid , sensorInfo, request);

            SensorMulticastServer.messageSender(payload);  // Use the server class to send the message
        }


    }

