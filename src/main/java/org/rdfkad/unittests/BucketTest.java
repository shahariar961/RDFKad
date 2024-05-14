//package org.rdfkad.unittests;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.rdfkad.Bucket;
//import org.rdfkad.packets.RoutingPacket;
//import org.rdfkad.tables.NodeConfig;
//import org.rdfkad.tables.RoutingTable;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class BucketTest {
//    private Bucket bucket;
//    private final int bucketCount = 13; // For 12-bit space
//
//    @BeforeEach
//    void setUp() {
//        // Reset NodeConfig singleton for testing
//        NodeConfig nodeConfig = NodeConfig.getInstance();
//        nodeConfig.resetNodeId();
//        nodeConfig.setNodeId("000101010001"); // Example 12-bit node ID
//
//        // Initialize the Bucket singleton with the required number of buckets
//        bucket = Bucket.getInstance(bucketCount);
//
//        // Add nodes to the routing table for testing
//        addTestNodesToRoutingTable();
//    }
//
//    private void addTestNodesToRoutingTable() {
//        ConcurrentHashMap<String, RoutingPacket> routingTable = RoutingTable.getInstance().getMap();
//        Random random = new Random();
//
//        for (int i = 0; i < 3000; i++) {
//            // Generate a random 12-bit node ID in binary format
//            String nodeId = String.format("%12s", Integer.toBinaryString(random.nextInt(1 << 12))).replace(' ', '0');
//            RoutingPacket packet = new RoutingPacket(8000 + i, "localhost", i);
//            routingTable.put(nodeId, packet);
//        }
//    }
//
//    @Test
//    void testSortIntoBuckets() {
//        bucket.sortIntoBuckets();
//
//        List<Set<String>> buckets = bucket.getBuckets();
//        assertEquals(13, buckets.size(), "Number of buckets should be 13");
//
//        // Print the number of nodes in each bucket
//        for (int i = 0; i < buckets.size(); i++) {
//            System.out.println("Bucket " + i + ": " + buckets.get(i).size() + " nodes");
//        }
//
//        // Check nodes are added to the right buckets
//        assertFalse(buckets.get(12).isEmpty(), "Expected nodes in bucket 12");
//        assertFalse(buckets.get(10).isEmpty(), "Expected nodes in bucket 10");
//    }
//    @Test
//    void testAddNodeToBucket() {
//        // Add a node to a specific bucket index
//        String testNodeId = "testNode";
//        int bucketIndex = 5;
//        bucket.addNodeToBucket(bucketIndex, testNodeId);
//
//        List<Set<String>> buckets = bucket.getBuckets();
//        assertTrue(buckets.get(bucketIndex).contains(testNodeId), "Node should be in bucket " + bucketIndex);
//    }
//
//    @Test
//    void testInvalidBucketIndex() {
//        String testNodeId = "testNode";
//
//        // Test out-of-range index
//        assertThrows(IndexOutOfBoundsException.class, () -> bucket.addNodeToBucket(bucketCount, testNodeId));
//        assertThrows(IndexOutOfBoundsException.class, () -> bucket.addNodeToBucket(-1, testNodeId));
//    }
//}
