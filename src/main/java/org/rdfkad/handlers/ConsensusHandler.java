package org.rdfkad.handlers;

import org.rdfkad.functions.XOR;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.tables.NodeConfig;
import org.rdfkad.tables.RoutingTable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;

public class ConsensusHandler {
    private String ownNodeId;
    private NodeConfig nodeConfig = NodeConfig.getInstance();

    public ConsensusHandler() {
        this.ownNodeId = nodeConfig.getNodeId();
    }

    /**
     * Selects the three closest nodes based on XOR distance and sends a payload to them.
     *
     * @param dataId The ID of the data to use for calculating XOR distance.
     */
    public void contactClosestNodes(String dataId) throws IOException {
        PriorityQueue<NodeDistancePair> closestNodes = findThreeClosestNodes();

        OutgoingConnectionHandler outgoingConnectionHandler = new OutgoingConnectionHandler();
        int nodesContacted = 0;

        while (!closestNodes.isEmpty() && nodesContacted < 3) {
            NodeDistancePair pair = closestNodes.poll();
            RoutingPacket routingPacket = RoutingTable.getInstance().getMap().get(pair.nodeId);
            if (routingPacket != null) {
                outgoingConnectionHandler.connectToNode("consensus", routingPacket, dataId);
                nodesContacted++;
            }
        }

        if (nodesContacted < 3) {
            System.out.println("Less than 3 nodes were available for consensus.");
        }
    }

    private PriorityQueue<NodeDistancePair> findThreeClosestNodes() {
        Comparator<NodeDistancePair> comparator = Comparator.comparing(pair -> pair.distance);
        PriorityQueue<NodeDistancePair> queue = new PriorityQueue<>(comparator);

        Map<String, RoutingPacket> routingTable = RoutingTable.getInstance().getMap();
        for (Map.Entry<String, RoutingPacket> entry : routingTable.entrySet()) {
            String nodeId = entry.getKey();
            if (!nodeId.equals(ownNodeId)) {
                BigInteger distance = XOR.Distance(ownNodeId, nodeId);
                queue.add(new NodeDistancePair(nodeId, distance));
            }
        }

        return queue;
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