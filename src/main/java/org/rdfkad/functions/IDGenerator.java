package org.rdfkad.functions;

import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.tables.RoutingTable;

import  java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IDGenerator{
    private static final Random random = new Random();
    private static ConcurrentHashMap<String, RoutingPacket> routingTable= RoutingTable.getInstance().getMap();

    public static String generateNodeId() {
        String nodeId;
        do {
            nodeId = generateRandomBinaryId();
        } while (routingTable.contains(nodeId));
        return nodeId;
    }

    private static String generateRandomBinaryId() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(random.nextBoolean() ? '1' : '0');
        }
        return sb.toString();
    }
}