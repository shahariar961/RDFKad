package org.rdfkad.unittests;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.rdfkad.functions.XOR;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.Bucket;

public class BucketTest {

    private final HashMap<String, RoutingPacket> routingTable = new HashMap<>();

    private void populateRoutingTable(int numberOfNodes) {
        for (int i = 0; i < numberOfNodes; i++) {
            String nodeId = generateRandomNodeId();
            RoutingPacket packet = new RoutingPacket(5000); // Replace 5000 with the actual port number
            routingTable.put(nodeId, packet);
        }
    }

    private String generateRandomNodeId() {
        Random random = new Random();
        BigInteger bigInt = new BigInteger(12, random);
        return bigInt.toString(2);
    }

//    @Test
//    public void testGetLeadingZeros() {
//        populateRoutingTable(100); // Populate the routing table with 10 nodes
//        String nodeId = generateRandomNodeId();
//        Bucket bucket = new Bucket(nodeId, routingTable, 160);
//        int leadingZeros = bucket.getLeadingZeros(12);
//        assertEquals(10, leadingZeros);
//    }

    //    @Test
//    public void testSortIntoBuckets() {
//        populateRoutingTable(300); // Populate the routing table with 10 nodes
//        String nodeId = generateRandomNodeId();
//        Bucket bucket = new Bucket(nodeId, routingTable, 12);
//        bucket.sortIntoBuckets();
//
//        // Add assertions to check if the buckets are sorted as expected
//        // This will depend on the entries you added to the routingTable
//    }
//    @Test
//    public void testPrintBuckets() {
//        populateRoutingTable(300); // Populate the routing table with 30 nodes
//        String nodeId = generateRandomNodeId();
//        Bucket bucket = new Bucket(nodeId, routingTable, 12);
//        bucket.sortIntoBuckets();
//
//        // Print the buckets
//        List<Set<String>> buckets = bucket.getBuckets();
//        for (int i = 0; i < ((List<?>) buckets).size(); i++) {
//            System.out.println("Bucket " + i + ": " + buckets.get(i));
//        }
//    }
//}
    @Test
    public void testPrintBuckets() {
        populateRoutingTable(30000); // Populate the routing table with 300 nodes
        String nodeId = generateRandomNodeId();  // This is the node ID used to initialize the bucket
        Bucket bucket = new Bucket(nodeId, routingTable, 13);
        bucket.sortIntoBuckets();

        // Print the buckets and XOR distances next to each node ID
        List<Integer> bucketSizes = bucket.getBucketSizes(); // This method now returns List<Integer> with sizes
        for (int i = 0; i < bucketSizes.size(); i++) {
            System.out.println("Bucket " + i + ": " + bucketSizes.get(i) + " nodes");
        }
    }
}
