package org.rdfkad.handlers;
//

import org.rdfkad.Bucket;
import org.rdfkad.functions.XOR;
import org.rdfkad.Node;
import org.rdfkad.packets.RoutingPacket;

import java.math.BigInteger;
import java.util.Set;

public class DataFinder {
    private Bucket bucketSystem;
    private String nodeId;
    private String dataId;

    public DataFinder(Bucket bucketSystem, String dataId , String nodeId){
        this.bucketSystem = bucketSystem;
        this.nodeId = nodeId;
        this.dataId = dataId;
    }

    /**
     * Find the bucket index for a given data ID based on the leading zeroes in the XOR distance.
     *
     * @param dataId The 12-bit binary data ID to find the bucket index for.
     * @return The bucket index where nodes closest to the dataId are stored.
     */
    public int findBucketIndex(String dataId) {
        // Calculate XOR distance between the node ID and the data ID
        BigInteger nodeIdBigInt = new BigInteger(nodeId, 2);
        BigInteger dataIdBigInt = new BigInteger(dataId, 2);
        BigInteger xorDistance = XOR.Distance(nodeIdBigInt.toString(2), dataIdBigInt.toString(2));

        // Calculate the bucket index based on the number of leading zeroes
        int bucketIndex = bucketSystem.getLeadingZeros(xorDistance);
        return bucketIndex;
    }

    /**
     * Retrieves the set of node IDs from the bucket that corresponds to the given data ID.
     *
     * @param dataId The 12-bit binary data ID to search for.
     * @return A set of node IDs from the corresponding bucket.
     */
    public Set<String> getNodesFromBucket(String dataId) {
        int bucketIndex = findBucketIndex(dataId);
        return bucketSystem.getBuckets().get(bucketIndex);
    }
}