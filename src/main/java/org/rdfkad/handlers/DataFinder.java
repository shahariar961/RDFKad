package org.rdfkad.handlers;

import org.rdfkad.functions.XOR;
import org.rdfkad.packets.RDFPacket;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.tables.DataTable;
import org.rdfkad.tables.NodeConfig;
import org.rdfkad.tables.RoutingTable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataFinder {
    private  DataTable dataTableMap = DataTable.getInstance();
    private ConcurrentHashMap<String, Object> dataTable;
    private  OutgoingConnectionHandler outgoingConnectionHandler = new OutgoingConnectionHandler();
    private RoutingTable routingTableMap = RoutingTable.getInstance();
    private ConcurrentHashMap<String, RoutingPacket> routingTable;
    private String ownNodeId= NodeConfig.getInstance().getNodeId();


    public DataFinder() {

    }
    public String findData(String dataId) throws IOException {
        dataTable = dataTableMap.getMap();
        routingTable = routingTableMap.getMap();
        String dataValue;

        // Step 1: Retrieve the composite data value

        if (dataTable.containsKey(dataId)) {
            dataValue=  dataTable.get(dataId).toString();
        } else {
            String closestNodeId = FindClosestNode(dataId);
            RoutingPacket routingPacket = routingTable.get(closestNodeId);
            try {
                outgoingConnectionHandler.connectToNode("find", routingPacket, dataId);
            } catch (IOException e) {
                e.printStackTrace();
            }
            dataTable = dataTableMap.getMap();
            dataValue = dataTable.get(dataId).toString();

        }
        //System.out.println("Data value: " + dataValue);
        return dataValue;

    }
    public String findCompositeData(String compositeDataId) throws IOException {
        dataTable = dataTableMap.getMap();
        routingTable = routingTableMap.getMap();
        String compositeValue;
        String objectValue ;

        // Step 1: Retrieve the composite data value

        if (dataTable.containsKey(compositeDataId)) {
            compositeValue = dataTable.get(compositeDataId).toString();
            //System.out.println("Composite data value: " + compositeValue);
            String[] dataIds = compositeValue.split(",");
            if (dataIds.length != 3) {
                throw new IllegalArgumentException("Composite data value does not contain exactly three data IDs: " + compositeValue);
            }
            // Step 3: Retrieve the values corresponding to these three data IDs
            String subjectId = dataIds[0];
            String predicateId = dataIds[1];
            String objectId = dataIds[2];
            try {
                dataTable.put(subjectId, findData(subjectId));
                dataTable.put(predicateId, findData(predicateId));
                dataTable.put(objectId, findData(objectId));
            }catch (IOException e){
                e.printStackTrace();
            }

        }else {
            String closestNodeId = FindClosestNode(compositeDataId);
            RoutingPacket routingPacket = routingTable.get(closestNodeId);
            outgoingConnectionHandler.connectToNode("find", routingPacket, compositeDataId);
            dataTable = dataTableMap.getMap();
            compositeValue = dataTable.get(compositeDataId).toString();
        }
        // Step 2: Split the composite data value to get three individual data IDs
        String[] dataIds = compositeValue.split(",");
        if (dataIds.length != 3) {
            throw new IllegalArgumentException("Composite data value does not contain exactly three data IDs: " + compositeValue);
        }
        // Step 3: Retrieve the values corresponding to these three data IDs
        String subjectId = dataIds[0];
        String predicateId = dataIds[1];
        String objectId = dataIds[2];
        try {
            dataTable.put(subjectId, findData(subjectId));
            dataTable.put(predicateId, findData(predicateId));
            dataTable.put(objectId, findData(objectId));


        } catch (IOException e) {
            e.printStackTrace();
        }
        objectValue = dataTable.get(objectId).toString();
        //System.out.println("Object value: " + objectValue);
        return objectValue;

    }

        private String FindClosestNode (String compositeDataId){
            BigInteger xorDistance = XOR.Distance(compositeDataId, ownNodeId);
            String closestNodeId = null;
            BigInteger smallestDistance = xorDistance;
            for (Map.Entry<String, RoutingPacket> entry : routingTable.entrySet()) {

                String otherNodeId = entry.getKey();
                BigInteger distance = XOR.Distance(compositeDataId, otherNodeId);
                // Compare distances, and skip updating if the node is itself
                if (distance.compareTo(smallestDistance) < 0) {
                    smallestDistance = distance;
                    closestNodeId = otherNodeId;
                }
            }
            return closestNodeId;
        }
    }
