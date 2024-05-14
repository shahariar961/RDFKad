package org.rdfkad.handlers;
//

import org.rdfkad.Bucket;
import org.rdfkad.functions.XOR;
import org.rdfkad.Node;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.tables.DataTable;
import org.rdfkad.tables.NodeConfig;
import org.rdfkad.tables.RoutingTable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DataFinder {
    private final List<Set<String>> buckets  = Bucket.getInstance().getBuckets();
    private String ownNodeId;

    private NodeConfig nodeConfig = NodeConfig.getInstance();

    private ConcurrentHashMap <String, Object> dataTable = DataTable.getInstance().getMap();

    public DataFinder(){

        this.ownNodeId = nodeConfig.getNodeId();

    }


    /**
     * Retrieves the set of node IDs from the bucket that corresponds to the given data ID.
     *
     * @param dataId The 12-bit binary data ID to search for.
     * @return A set of node IDs from the corresponding bucket.
     */


    public void findData(String dataId) throws IOException {
        BigInteger xorDistance = XOR.Distance(dataId, ownNodeId);
        int leadingZeroIndex = Bucket.getLeadingZeros(xorDistance);
        Set<String> targetBucket = buckets.get(leadingZeroIndex);
        String closestNodeId = null;
        BigInteger smallestDistance = xorDistance;
        if (targetBucket != null && !targetBucket.isEmpty()) {
            System.out.println("Accessing bucket index");
            for (String otherNodeId : targetBucket) {
                BigInteger distance = XOR.Distance(dataId, otherNodeId);
                // Compare distances, and skip updating if the node is itself
                if (distance.compareTo(smallestDistance) < 0) {
                    smallestDistance = distance;
                    closestNodeId = otherNodeId;
                }
            }
        } else {
            System.out.println("Bucket empty, accessing routing table");
            // Fallback: Iterate through the entire routing table if the target bucket is empty
            for (Map.Entry<String, RoutingPacket> entry : RoutingTable.getInstance().getMap().entrySet()) {

                String otherNodeId = entry.getKey();
                BigInteger distance = XOR.Distance(dataId, otherNodeId);
                // Compare distances, and skip updating if the node is itself
                if (distance.compareTo(smallestDistance) < 0) {
                    smallestDistance = distance;
                    closestNodeId = otherNodeId;
                }
            }
        }
        if (closestNodeId == null || smallestDistance.equals(xorDistance)) {
            System.out.println("Node is closest to Data id, finding node locally.");
            if (dataTable.containsKey(dataId)) {
                System.out.println("Data Found Locally");
                System.out.println(dataTable.get(dataId));
            } else {
                System.out.println("Data Not Found Locally");
            }

            // Store the data locally
            // Example: localDataStore.put(dataId, payload);
        } else {
            RoutingPacket routingPacket = RoutingTable.getInstance().getMap().get(closestNodeId);
            System.out.println("Querying Node" + closestNodeId + "For Data " + routingPacket.getHost() + ":" + routingPacket.getPort());
            OutgoingConnectionHandler outgoingConnectionHandler = new OutgoingConnectionHandler();
            outgoingConnectionHandler.connectToNode("find", routingPacket, dataId);
        }
    }
}