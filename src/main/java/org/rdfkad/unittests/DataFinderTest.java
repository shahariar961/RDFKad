package org.rdfkad.unittests;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.rdfkad.Bucket;
import org.rdfkad.handlers.DataFinder;
import org.rdfkad.functions.XOR;
import org.rdfkad.packets.RoutingPacket;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class DataFinderTest {

    private final HashMap<String, RoutingPacket> routingTable = new HashMap<>();

    private void populateRoutingTable(int numberOfNodes) {
        Random random = new Random();
        for (int i = 0; i < numberOfNodes; i++) {
            String nodeId = generateRandomNodeId(random);
            RoutingPacket packet = new RoutingPacket(5000); // Port number
            routingTable.put(nodeId, packet);
        }
    }

    private String generateRandomNodeId(Random random) {
        BigInteger bigInt = new BigInteger(12, random); // Generate 12-bit random number
        return String.format("%12s", bigInt.toString(2)).replace(' ', '0'); // Pad with zeros to ensure 12 bits
    }

    @Test
    public void testDataFinderFunctionality() {
        populateRoutingTable(30000); // Populate the routing table with 300 nodes
        String currentNodeId = generateRandomNodeId(new Random()); // Generate a current node ID
        Bucket bucket = new Bucket(currentNodeId, routingTable, 13);
        bucket.sortIntoBuckets(); // Sort nodes into buckets

        String randomDataId = generateRandomNodeId(new Random()); // Generate a random data ID
        DataFinder dataFinder = new DataFinder(bucket,randomDataId ,currentNodeId);
        int bucketIndex = dataFinder.findBucketIndex(randomDataId); // Find the bucket index for the data ID
        Set<String> nodeIdsInBucket = bucket.getBuckets().get(bucketIndex);

        System.out.println("Random Data ID: " + randomDataId);
        System.out.println("Closest Bucket Index: " + bucketIndex);
        System.out.println("Nodes in Bucket: " + nodeIdsInBucket);

        // Identify which node in the bucket is closest to the current node ID
        BigInteger currentNodeIdBigInt = new BigInteger(currentNodeId, 2);
        String closestNodeId = null;
        BigInteger smallestDistance = null;

        for (String nodeId : nodeIdsInBucket) {
            BigInteger nodeIdBigInt = new BigInteger(nodeId, 2);
            BigInteger distance = currentNodeIdBigInt.xor(nodeIdBigInt);
            if (smallestDistance == null || distance.compareTo(smallestDistance) < 0) {
                smallestDistance = distance;
                closestNodeId = nodeId;
            }
        }

        if (closestNodeId != null) {
            System.out.println("Closest Node in Bucket to Current Node ID: " + closestNodeId);
        } else {
            System.out.println("No nodes found in the closest bucket.");
        }

        assertTrue(closestNodeId != null, "There should be at least one node in the closest bucket.");
    }
}
