package org.rdfkad;

import org.rdfkad.*;
import org.rdfkad.datahandlers.hash;
import org.rdfkad.functions.IDGenerator;
import org.rdfkad.functions.XOR;
import org.rdfkad.packets.HashPacket;
import org.rdfkad.packets.Payload;
import org.rdfkad.packets.RDFDataPacket;
import org.rdfkad.packets.RoutingPacket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Node {
    private BlockingQueue<List<String>> dataQueue; // Serializable list
    private ServerSocket serverSocket;
    private String nodeId="null";
    private static Integer nodePort;
    private HashMap<String, RoutingPacket> routingTableMap;

    private Hashtable<String,Object> dataTable;
    private HashMap<String,RoutingPacket> Bucket;

    private Map<String, Object> overlayTable = new HashMap<>();

    private boolean flag= false;

    public Node() {

        this.dataQueue = new ArrayBlockingQueue<>(10);
        this.dataTable = new Hashtable<>();
        this.Bucket=new HashMap<>();
        this.routingTableMap=new HashMap<>();
// Adjust the capacity as needed
    }

    public BlockingQueue<List<String>> getDataQueue() {
        return dataQueue;
    }

    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Node listening on port " + port);

            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.submit(this::listenForConnections);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenForConnections() {

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                receiveData(clientSocket);


                System.out.println("Accepted connection from Node on port " + clientSocket.getPort());

                // Receive data from the connected node
//                receiveData(clientSocket);

//                Payload receivedObject = (Payload)inputStream.readObject();




            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    public void receiveData(Socket socket) throws IOException {
        try (ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())) {

            Object received = inputStream.readObject();
            if (received instanceof String){
                String packet = (String) received;
                System.out.println(packet);
                RDFDataPacket RDFPacket = new RDFDataPacket(packet);
                System.out.println(RDFPacket.subject + " " + RDFPacket.predicate + " " + RDFPacket.Object);

                overlayInitiator(RDFPacket);

            }
            if (received instanceof RDFDataPacket) {
                RDFDataPacket RDFPacket = (RDFDataPacket) received;
                System.out.println(RDFPacket.subject + " " + RDFPacket.predicate + " " + RDFPacket.Object);

                overlayInitiator(RDFPacket);
            }
            if (received instanceof Payload){
                Payload receivedMessage = (Payload) received;
                System.out.println(receivedMessage.request);
                if (!routingTableMap.containsKey(receivedMessage.nodeId)){
                    System.out.println("Unknown Node Connecting, inserting Node Id in Routing Table");
                    RoutingPacket packet =new RoutingPacket(receivedMessage.port, receivedMessage.flag);
                    routingTableMap.put(receivedMessage.nodeId,packet);
                    KBucket();
                }
                if(receivedMessage.request.equals("get")) {
                    System.out.println("Data Get Request");
                    if(dataTable.containsKey(receivedMessage.dataId)) {
                        System.out.println("Data Found in Hash Table");
                        outputStream.writeObject(dataTable.get(receivedMessage.dataId));
                        System.out.println("Sending Data");
                        outputStream.flush();


                    }else {
                        outputStream.writeObject("Data not found");
                    }
                }
                if (receivedMessage.request.equals("find")) {
                    System.out.println("Sending Routing Data");
                    outputStream.writeObject(routingTableMap);
                }
                if (receivedMessage.request.equals("nearby")){
                    System.out.println("Sending Nearby Node Info");
                    HashMap<String,Integer> xorDistances = new HashMap<String,Integer>();
                    for (Map.Entry<String, RoutingPacket> entry : routingTableMap.entrySet()) {
                        String nodeId = entry.getKey();
                        int distance = XOR.Distance(receivedMessage.nodeId, nodeId); // Assuming XOR is the class with calculateXORDistance method
                        xorDistances.put(nodeId, distance);
                    }

                    // Select the 8 smallest XOR distances

                    HashMap<String, Integer> closestNodes = new HashMap<>();
                    closestNodes = xorDistances.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByValue())
                            .limit(8)
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (e1, e2) -> e1,
                                    HashMap::new
                            ));

                    // Print the closest nodes
                    System.out.println("8 Nodes with Closest XOR Distances:");
                    HashMap<String, Integer> sendingTable = new HashMap<>();
                    for (Map.Entry<String, Integer> entry : closestNodes.entrySet()) {


                        String nodeId = entry.getKey();
                        RoutingPacket packet=routingTableMap.get(nodeId);
                        Integer nodePort= packet.port;
                        sendingTable.put(nodeId,nodePort);


                    }
                    outputStream.writeObject(sendingTable);
                    System.out.println("Nearby Nodes Sent");

                }
            }if (received instanceof HashMap<?,?>){
                HashMap<String,RoutingPacket> tempTable= (HashMap<String, RoutingPacket>) received;
                System.out.println("Received routing table");

                for (Map.Entry<String, RoutingPacket> entry : tempTable.entrySet()) {
                    String key = entry.getKey();
                    RoutingPacket value = entry.getValue();

                    // Check if key exists in map2
                    if (!routingTableMap.containsKey(key)) {
                        // Key doesn't exist in map2, insert the entry
                        routingTableMap.put(key, value);
                        System.out.println("New Nodes Added ID:" + key);

                    }
                }
                KBucket();
            }



        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        socket.close();
    }
    public static void sendRDF( RoutingPacket RoutingInfo, RDFDataPacket packet) {
        try (Socket socket = new Socket("localhost", RoutingInfo.port);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

//            String serializedPacket = packet.serialize();
            oos.writeObject(packet);
            System.out.println("RDF Packet sent: " + packet);

        } catch (IOException e) {
            System.err.println("Error sending RDF packet: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void connectToBootstrapServer(String host, int port) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
            String nodeInfo = (nodeId + "," + nodePort);
            outputStream.writeObject(nodeInfo);

            // Simulate generating a unique node ID (you can implement this based on your requirements)

//                // Simulate creating a routing table for the node
//                // Assumes the node listens on its own port
            Object receivedObject = inputStream.readObject();

//                } else
            if (receivedObject instanceof String) {
                String receivedNodeId=(String) receivedObject;
                if (nodeId.equals("null")){
                    nodeId=receivedNodeId;
                    System.out.println("Recieved ID: " + nodeId);
                    System.out.println("------------------------");
                }

            }
            Object receivedObject2 = inputStream.readObject();
            if (receivedObject2 instanceof HashMap<?, ?>) {
                HashMap<String, RoutingPacket> recievedHashMap = (HashMap<String, RoutingPacket>) receivedObject2;
                if (!recievedHashMap.isEmpty()){
                    routingTableMap = recievedHashMap;
                }
                for (HashMap.Entry<String, RoutingPacket> entry : recievedHashMap.entrySet()) {
                    String nodeId2 = entry.getKey();
                    RoutingPacket packet = entry.getValue();
                    System.out.println("Node ID: " + nodeId2);
                    System.out.println("Data Port: " + packet.port);
                    System.out.println("------------------------");
                }
            }
//                // Send the node ID and routing table to the bootstrap server
//                //outputStream.writeObject(nodeId);
//                //outputStream.writeObject(routingTable);
//
//                // Receive acknowledgment from the bootstrap server
////            String acknowledgment = (String) inputStream.readObject();
////            System.out.println("Bootstrap Server Response: " + acknowledgment);
//            }
            KBucket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void connectToNode(String host, RoutingPacket packet, String dataId,String query) {
        try
        {
            Socket socket = new Socket(host, packet.port);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            // Send the node ID and routing table to the connected node
//            String nodeInfo = (nodeId + "," + port);
//            outputStream.writeObject(nodeInfo);
//            outputStream.writeObject(routingTable);
            Payload messagePayload= new Payload(query,nodeId,nodePort,dataId,flag);
//            System.out.println(messagePayload.request);
            outputStream.writeObject(messagePayload);
            Object receivedObject = null;
            try {
                receivedObject = inputStream.readObject();
                if (receivedObject instanceof String){
                    System.out.println(receivedObject);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }



        }  catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException  e) {
            throw new RuntimeException(e);
        }
    }
    public void connectToNode(String host, RoutingPacket packet,String query) {
        try
        {
            Socket socket = new Socket(host, packet.port);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            // Send the node ID and routing table to the connected node
//            String nodeInfo = (nodeId + "," + port);
//            outputStream.writeOQject(nodeInfo);
//            outputStream.writeObject(routingTable);
            Payload messagePayload= new Payload(query,nodeId,nodePort,flag);
//            System.out.println(messagePayload.request);
            outputStream.writeObject(messagePayload);
            Object receivedObject = null;
            try {
                receivedObject = inputStream.readObject();
                if (receivedObject instanceof String){
                    System.out.println(receivedObject);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }



        }  catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException  e) {
            throw new RuntimeException(e);
        }
    }

    // Assume dataAddress is a binary string
    public void searchData(String dataAddress) {
        // Calculate XOR distance between the target data address and IDs in the routing table
        int minDistance = Integer.MAX_VALUE;
        String closestNodeId = null;

        for (Map.Entry<String, RoutingPacket> entry : routingTableMap.entrySet()) {
            String nodeId = entry.getKey();
            int distance = XOR.Distance(dataAddress, nodeId);

            if (distance < minDistance) {
                minDistance = distance;
                closestNodeId = nodeId;
            }
        }

        // Contact the closest node for data retrieval
        if (closestNodeId != null) {
            connectToNode("localhost",routingTableMap.get(closestNodeId), dataAddress,"get");
        } else {
            System.out.println("No nodes in the routing table. Unable to search for data.");
        }
    }
    //Needs to be changed !!

    private void inputData(String dataId, String dataValue){
        dataTable.put(dataId,dataValue);
        System.out.println("Stored Data ID:" +dataId + ", Value:" + dataValue);
    }
    private void KBucket() {
        // Initialize a priority queue to keep track of the lowest distances
        PriorityQueue<Map.Entry<String, Integer>> minHeap = new PriorityQueue<>((e1, e2) -> e1.getValue() - e2.getValue());
        if (routingTableMap.isEmpty()){
            System.out.println("Empty Routing Table");
        }else {
            for (Map.Entry<String, RoutingPacket> entry : routingTableMap.entrySet()) {
                String id = entry.getKey();
                Integer distance = XOR.Distance(nodeId, id);
                Map.Entry<String, Integer> newEntry = new AbstractMap.SimpleEntry<>(id, distance);
                // Add the current entry to the priority queue
                minHeap.offer(newEntry);

                // If the size of the priority queue exceeds 8, remove the entry with the highest distance
                if (minHeap.size() > 8) {
                    minHeap.poll();
                }
            }
            while (!minHeap.isEmpty()) {
                Map.Entry<String, Integer> entry = minHeap.poll();
                Integer distance = entry.getValue();
                RoutingPacket packet = routingTableMap.get(entry.getKey());
                RoutingPacket distancePacket = new RoutingPacket(packet.port, packet.flag, packet.distance);
                Bucket.put(entry.getKey(), distancePacket);
                System.out.println("Added node into bucket" + entry.getKey());
            }
            if (Bucket.size() > 8) {
                // Initialize a priority queue to keep track of the shortest packet distances
                PriorityQueue<Map.Entry<String, RoutingPacket>> distanceMinHeap = new PriorityQueue<>(
                        (e1, e2) -> e1.getValue().distance - e2.getValue().distance);

                // Add all entries from the Bucket to the priority queue
                distanceMinHeap.addAll(Bucket.entrySet());

                // Clear the Bucket
                Bucket.clear();
                System.out.println("Restructuring Bucket");

                // Add only the top 8 entries with the shortest packet distance back to the Bucket
                for (int i = 0; i < 8; i++) {
                    Map.Entry<String, RoutingPacket> entry = distanceMinHeap.poll();
                    if (entry != null) {
                        Bucket.put(entry.getKey(), entry.getValue());
                        System.out.println("Adding no into bucket" + entry.getKey());
                    }
                }
            }
        }
    }


    // Utility function to compute SHA-1 hash

//    public HashPacket storeRDF(RDFDataPacket packet) {
//        // Computing SHA-1 hashes for each component
//        String subjectHash = hash.computeSHA1(packet.subject);
//        String predicateHash = hash.computeSHA1(packet.predicate);
//        String objectHash = hash.computeSHA1(packet.Object);
//
//        // Caching each component of the RDF packet using its SHA-1 hash as key
//        overlayTable.put(subjectHash, packet.subject);
//        overlayTable.put(predicateHash, packet.predicate);
//        overlayTable.put(objectHash, packet.Object);
//
//
//        return new HashPacket(objectHash,predicateHash,subjectHash);
//    }


    public void overlayInitiator(RDFDataPacket packet) {
        if (flag) {
            System.out.println("Hashpacket sent");
            HashPacket hashInfo=storeRDF(packet);
            System.out.println("Hash Recieved");
            String dataId= IDGenerator.generateID(nodeId,dataTable);
            System.out.println("ID Recieved" + dataId);
            dataTable.put(dataId,hashInfo);
            System.out.println("RDF Data stored in table ID:" + dataId );

        } else {
            Map.Entry<String, RoutingPacket> entryFlag = null;
            for (Map.Entry<String, RoutingPacket> entry : Bucket.entrySet()) {
                RoutingPacket routingInfo = entry.getValue();
                if (routingInfo.flag) {
                    entryFlag = entry;
                    break;
                }
            }
            if (entryFlag != null) {
                sendRDF(entryFlag.getValue(),packet);
            } else {
                flag = true;
                overlayInitiator(packet);
            }

        }
    }
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
    public  void connectNearby(){
        int minDistance = Integer.MAX_VALUE;
        String closestNodeId = null;
        if (nodeId==null){
            System.out.println("Please enter network first");
        }else {
            if (routingTableMap.isEmpty()){
                connectToBootstrapServer("localhost",9090);
                System.out.println("Populating RoutingTable");
            }
            for (Map.Entry<String,RoutingPacket> entry : routingTableMap.entrySet()) {

                if (!entry.getKey().equals(nodeId)){
                    int distance = XOR.Distance(nodeId, entry.getKey());
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestNodeId = entry.getKey();
                    }

                }

            }
            if (!closestNodeId.equals("null")) {
                System.out.println("Contacting node ID:" + closestNodeId);
                connectToNode("localhost", routingTableMap.get(closestNodeId), "nearby");
            }else {
                System.out.println("No other nodes known");
            }


        }
    }
    public static void main(String[] args) {
        Node node = new Node();
        if (args.length != 1) {
            System.out.println("Usage: java Node <port>");
            System.exit(1);
        }
        nodePort = Integer.parseInt(args[0]);
        // Start server on Node (replace 8080 with the desired port)
        node.startServer(nodePort);


        // CLI for connecting to other nodes or Bootstrap Server
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("Enter command (search data <data Id> or connect server <port> or store data <Id> <Value>): ");
                String command = reader.readLine().trim();

                if (command.startsWith("search data")) {
                    String[] parts = command.split("\\s+");
                    if (parts.length == 3) {

//                        int port = Integer.parseInt(parts[2]);
                        String dataId=parts[2];
                        node.searchData(dataId);
                    } else {
                        System.out.println("Invalid command. Usage: connect node <host> <port>");
                    }
                }else if (command.startsWith("connect server")) {

                    node.connectToBootstrapServer("localhost", 9090);
                }
                else if (command.startsWith("store data")) {
                    String[] parts = command.split("\\s+");
                    if (parts.length == 4) {
                        String dataId = parts[2];
                        String dataValue=parts[3];
                        node.inputData(dataId, dataValue);
                    } else {
                        System.out.println("Invalid command. Usage: connect server <host> <port>");
                    }
                } else if (command.startsWith("connect nearby")) {

                    node.connectNearby();
                } else if (command.startsWith("show routing")) {
                    if (node.routingTableMap.isEmpty()){
                        System.out.println("Routing table is empty");
                    }else {
                        for (Map.Entry<String,RoutingPacket> entry : node.routingTableMap.entrySet()){
                            System.out.println(entry.getKey());
                        }
                    }

                } else {
                    System.out.println("Unknown command. Available commands: connect node <host> <port> or connect server <host> <port>");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


