
package org.rdfkad;

import org.rdfkad.functions.XOR;
import org.rdfkad.packets.RoutingPacket;

import java.math.BigInteger;
import java.util.*;



public class Bucket {
    private final String nodeId;
    private final HashMap<String, RoutingPacket> routingTable;
    private final List<Set<String>> buckets;

    public Bucket(String nodeId, HashMap<String, RoutingPacket> routingTable, int bucketCount) {
        this.nodeId = nodeId;
        this.routingTable = routingTable;
        this.buckets = new ArrayList<>(bucketCount);
        for (int i = 0; i < bucketCount; i++) {
            this.buckets.add(new HashSet<>());
        }
    }

    public void sortIntoBuckets() {
        for (String otherNodeId : routingTable.keySet()) {
            BigInteger distance = XOR.Distance(this.nodeId, otherNodeId);
            int bucketIndex = getLeadingZeros(distance);
            buckets.get(bucketIndex).add(otherNodeId);
        }
    }

    // Adjusted to a 12-bit identifier space
    public int getLeadingZeros(BigInteger distance) {
        int highestSetBit = distance.bitLength() - 1;  // Calculates the highest set bit
        return 11 - highestSetBit ;  // Adjust the calculation for a 12-bit space
    }

    public List<Integer> getBucketSizes() {
        List<Integer> sizes = new ArrayList<>();
        for (Set<String> bucket : buckets) {
            sizes.add(bucket.size());
        }
        return sizes;
    }
}