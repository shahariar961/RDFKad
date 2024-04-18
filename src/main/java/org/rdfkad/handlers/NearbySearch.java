package org.rdfkad.handlers;

import org.rdfkad.functions.XOR;
import org.rdfkad.packets.RoutingPacket;

import java.util.Map;
//
//public class NearbySearch {
//    public  void connectNearby(){
//        int minDistance = Integer.MAX_VALUE;
//        String closestNodeId = null;
//        if (nodeId==null){
//            System.out.println("Please enter network first");
//        }else {
//            if (routingTableMap.isEmpty()){
//                connectToBootstrapServer("localhost",9090);
//                System.out.println("Populating RoutingTable");
//            }
//            for (Map.Entry<String, RoutingPacket> entry : routingTableMap.entrySet()) {
//
//                if (!entry.getKey().equals(nodeId)){
//                    int distance = XOR.Distance(nodeId, entry.getKey());
//                    if (distance < minDistance) {
//                        minDistance = distance;
//                        closestNodeId = entry.getKey();
//                    }
//
//                }
//
//            }
//            if (!closestNodeId.equals("null")) {
//                System.out.println("Contacting node ID:" + closestNodeId);
//                connectToNode("localhost", routingTableMap.get(closestNodeId), "nearby");
//            }else {
//                System.out.println("No other nodes known");
//            }
//
//
//        }
//    }
//}
