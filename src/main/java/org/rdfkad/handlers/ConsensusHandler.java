package org.rdfkad.handlers;

import org.rdfkad.functions.XOR;
import org.rdfkad.packets.Payload;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.tables.AlarmMatrixObject;
import org.rdfkad.tables.RoutingTable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class ConsensusHandler {
    private RoutingTable routingMap = RoutingTable.getInstance();
    private ConcurrentHashMap<String, RoutingPacket> routingTable;
    private OutgoingConnectionHandler outgoingConnectionHandler = new OutgoingConnectionHandler();

    public boolean runConsensusAlgorithm( List<AlarmMatrixObject> alarmingNeighbors) {
        routingTable = routingMap.getMap();


        List<Payload> uniqueNodes = alarmingNeighbors.stream()
                .map(alarm -> {
                    String nodeId = null;
                    for (Map.Entry<String, RoutingPacket> entry : routingTable.entrySet()) {
                        if (entry.getValue().getMulticastId() == alarm.getMulticastId()) {
                            nodeId = entry.getKey();
                            break;
                        }
                    }
                    return new Payload(nodeId, alarm.getDataAddress());
                })
                .distinct()
                .collect(Collectors.toList());

        CountDownLatch latch = new CountDownLatch(uniqueNodes.size() * 2); // Latch for each request to two nodes

        for (Payload nodeData : uniqueNodes) {
            // Find the nearest two nodes for each alarming neighbor
            List<RoutingPacket> nearestTwoNodes = findNearestNodes(nodeData.getNodeId(), 2);
            for (RoutingPacket node : nearestTwoNodes) {
                sendConsensusRequest(node, nodeData.getDataId(), latch);
            }
        }

        try {
            latch.await();
            return true; // Wait for all consensus requests to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Latch interrupted: " + e.getMessage());
            return false;
        }
    }

    private List<RoutingPacket> findNearestNodes(String nodeId, int numberOfNodes) {
        return routingTable.entrySet().stream()
                .sorted((entry1, entry2) -> {
                    BigInteger distance1 = XOR.Distance(nodeId, entry1.getKey());
                    BigInteger distance2 = XOR.Distance(nodeId, entry2.getKey());
                    return distance1.compareTo(distance2);
                })
                .limit(numberOfNodes)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private void sendConsensusRequest(RoutingPacket node, String dataId, CountDownLatch latch) {
        try {
            boolean consensus = outgoingConnectionHandler.connectConsensus("consensus", node, dataId);
            if (consensus) {
                latch.countDown(); // Decrement the latch count after the request completes
            } else {
                throw new Exception("Consensus request failed.");
            }
        } catch (IOException e) {
            System.out.println("Error sending consensus request: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
