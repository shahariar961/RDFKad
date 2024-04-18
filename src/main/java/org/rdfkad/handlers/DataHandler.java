package org.rdfkad.handlers;

import org.rdfkad.functions.XOR;
import org.rdfkad.packets.HashPacket;
import org.rdfkad.packets.RDFDataPacket;
import org.rdfkad.packets.RoutingPacket;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class DataHandler {
    private HashMap<String, RoutingPacket> routingTableMap;
    private Hashtable<String,Object> dataTable;
    private Map<String, Object> overlayTable;
    public void searchData(String dataAddress) {
        // Calculate XOR distance between the target data address and IDs in the routing table
        int minDistance = Integer.MAX_VALUE;
        String closestNodeId = null;

        for (Map.Entry<String, RoutingPacket> entry : routingTableMap.entrySet()) {
            String nodeId = entry.getKey();
            BigInteger distance = XOR.Distance(dataAddress, nodeId);

//            if (distance < minDistance) {
//                minDistance = distance;
//                closestNodeId = nodeId;
//            }
        }

        // Contact the closest node for data retrieval
        if (closestNodeId != null) {
            OutgoingConnectionHandler.connectToNode("localhost",routingTableMap.get(closestNodeId), dataAddress,"get");
        } else {
            System.out.println("No nodes in the routing table. Unable to search for data.");
        }
    }
//    private void inputData(String dataId, String dataValue){
//        data Table.put(dataId,dataValue);
//        System.out.println("Stored Data ID:" +dataId + ", Value:" + dataValue);
//    }
    public HashPacket storeRDF(RDFDataPacket packet) {
        // Computing SHA-1 hashes for each component
        String subjectHash = hash.computeSHA1(packet.subject);
        String predicateHash = hash.computeSHA1(packet.predicate);
        String objectHash = hash.computeSHA1(packet.Object);

        // Caching each component of the RDF packet using its SHA-1 hash as key
        overlayTable.put(subjectHash, packet.subject);
        overlayTable.put(predicateHash, packet.predicate);
        overlayTable.put(objectHash, packet.Object);


        return new HashPacket(objectHash,predicateHash,subjectHash);
    }
}
