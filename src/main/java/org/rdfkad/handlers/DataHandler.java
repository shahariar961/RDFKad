package org.rdfkad.handlers;

import org.rdfkad.Bucket;
import org.rdfkad.functions.XOR;
import org.rdfkad.packets.HashPacket;
import org.rdfkad.packets.RDFPacket;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.tables.NodeConfig;
import org.rdfkad.tables.RoutingTable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataHandler {
    private final String ownNodeId = NodeConfig.getInstance().getNodeId();

    private final List <Set<String>> bucket;


    public DataHandler() {

        this.bucket = Bucket.getInstance().getBuckets();

    }

    public void sendData(String dataId, Object payload) throws IOException {

        // Calculate the XOR distance between the data ID and own node ID
        BigInteger xorDistance = XOR.Distance(dataId, ownNodeId);
        int leadingZeroIndex = Bucket.getLeadingZeros(xorDistance);

        Set<String> targetBucket = bucket.get(leadingZeroIndex);

        String closestNodeId = null;
        BigInteger smallestDistance = xorDistance; // Start with own distance to data ID

        // Check if the target bucket is empty or not
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

        // If no closest node is found, or the closest node is the local node itself
        if (closestNodeId == null || smallestDistance.equals(xorDistance)) {
            System.out.println("Data ID is closest to the local node. Storing only locally.");
            // Store the data locally
            // Example: localDataStore.put(dataId, payload);
        } else {
            RoutingPacket routingPacket = RoutingTable.getInstance().getMap().get(closestNodeId);
            System.out.println("Data ID is closest to node " + closestNodeId + ". Forwarding to node " + routingPacket.getHost() + ":" + routingPacket.getPort());
            OutgoingConnectionHandler outgoingConnectionHandler = new OutgoingConnectionHandler();
            outgoingConnectionHandler.connectToNode("store", routingPacket, dataId);
        }
    }


}
