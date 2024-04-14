
package org.rdfkad;

import java.util.*;

public class Bucket {
    private final String nodeId;
    private final HashMap<String, RoutingPacket> routingTable;
    private final List<Set<String>> buckets;

    public Bucket(String nodeId, HashMap<String, RoutingPacket> routingTable, int bucketCount) {
        this.nodeId = nodeId;
        this.routingTable = routingTable;
        this.buckets = new ArrayList<>(Collections.nCopies(bucketCount, new HashSet<>()));
        sortIntoBuckets();
    }

    private void sortIntoBuckets() {
        for (String otherNodeId : routingTable.keySet()) {
            int distance = getDistance(this.nodeId, otherNodeId);
            int bucketIndex = getLeadingZeros(distance);
            buckets.get(bucketIndex).add(otherNodeId);
        }
    }

    private int getDistance(String nodeId1, String nodeId2) {
        // Assuming nodeId1 and nodeId2 are binary strings
        int xor = Integer.parseInt(nodeId1, 2) ^ Integer.parseInt(nodeId2, 2);
        return xor;
    }

    private int getLeadingZeros(int number) {
        return Integer.numberOfLeadingZeros(number);
    }

    public List<Set<String>> getBuckets() {
        return buckets;
    }
}