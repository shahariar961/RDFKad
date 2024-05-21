package org.rdfkad.handlers;

import org.rdfkad.functions.XOR;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.tables.AlarmMatrixObject;
import org.rdfkad.tables.NodeConfig;
import org.rdfkad.tables.RoutingTable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class ConsensusHandler {
    private String ownNodeId;
    private NodeConfig nodeConfig = NodeConfig.getInstance();

    public ConsensusHandler() {
        this.ownNodeId = nodeConfig.getNodeId();
    }

    /**
     * Selects the closest node based on XOR distance for the node's own ID and sends a payload to them.
     *
     * @param alarmMatrixObjects The list of AlarmMatrixObject to process.
     */
    public void processAlarmMatrixObjects(List<AlarmMatrixObject> alarmMatrixObjects) {
        OutgoingConnectionHandler outgoingConnectionHandler = new OutgoingConnectionHandler();

        for (AlarmMatrixObject alarmMatrixObject : alarmMatrixObjects) {
            String dataAddress = alarmMatrixObject.getDataAddress();
            String closestNodeId = findClosestNode(ownNodeId); // Use the node's own ID to find the closest node

            if (closestNodeId != null) {
                RoutingPacket routingPacket = RoutingTable.getInstance().getMap().get(closestNodeId);
                if (routingPacket != null) {
                    try {
                        outgoingConnectionHandler.connectToNode("consensus", routingPacket, dataAddress);
                        System.out.println("Sent consensus request to node: " + closestNodeId + " for data address: " + dataAddress);
                    } catch (IOException e) {
                        System.out.println("Failed to send consensus request to node: " + closestNodeId);
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("No closest node found for data address: " + dataAddress);
            }
        }
    }

    /**
     * Finds the closest node based on XOR distance for the given node ID.
     *
     * @param nodeId The node ID to use for calculating XOR distance.
     * @return The node ID of the closest node.
     */
    private String findClosestNode(String nodeId) {
        Comparator<NodeDistancePair> comparator = Comparator.comparing(pair -> pair.distance);
        PriorityQueue<NodeDistancePair> queue = new PriorityQueue<>(comparator);

        Map<String, RoutingPacket> routingTable = RoutingTable.getInstance().getMap();
        for (Map.Entry<String, RoutingPacket> entry : routingTable.entrySet()) {
            String otherNodeId = entry.getKey();
            if (!otherNodeId.equals(ownNodeId)) {
                BigInteger distance = XOR.Distance(nodeId, otherNodeId);
                queue.add(new NodeDistancePair(otherNodeId, distance));
            }
        }

        if (!queue.isEmpty()) {
            return queue.poll().nodeId;
        }

        return null;
    }

    private static class NodeDistancePair {
        String nodeId;
        BigInteger distance;

        NodeDistancePair(String nodeId, BigInteger distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }
    }
}
