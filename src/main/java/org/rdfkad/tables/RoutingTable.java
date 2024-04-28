package org.rdfkad.tables;


import org.rdfkad.packets.RoutingPacket;

import java.util.concurrent.ConcurrentHashMap;

public class RoutingTable {
    // Single instance of RoutingTable
    private static RoutingTable single_instance = null;

    // The ConcurrentHashMap for the routing table
    private ConcurrentHashMap<String, RoutingPacket> routingMap;

    // Private constructor to restrict instantiation from other classes
    private RoutingTable() {
        routingMap = new ConcurrentHashMap<>();
    }

    // Static method to get the instance of the RoutingTable class
    public static RoutingTable getInstance() {
        if (single_instance == null) {
            synchronized (RoutingTable.class) {
                if (single_instance == null) {
                    single_instance = new RoutingTable();
                }
            }
        }
        return single_instance;
    }

    // Method to get the concurrent hash map
    public ConcurrentHashMap<String, RoutingPacket> getMap() {
        return routingMap;
    }

    // Additional methods to manipulate the routing table can be added here
}