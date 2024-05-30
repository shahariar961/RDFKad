package org.rdfkad;

import org.rdfkad.handlers.DataFinder;
import org.rdfkad.handlers.IncomingConnectionHandler;
import org.rdfkad.handlers.OutgoingConnectionHandler;
import org.rdfkad.multicast.SensorMulticastReceiver;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.tables.DataTable;
import org.rdfkad.tables.NodeConfig;
import org.rdfkad.tables.RoutingTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Node {
    private ServerSocket serverSocket;
    private static ConcurrentHashMap<String, RoutingPacket> routingTable = RoutingTable.getInstance().getMap();
    private static ConcurrentHashMap<String, Object> dataTable = DataTable.getInstance().getMap();
    private static NodeConfig nodeConfig = NodeConfig.getInstance();
    private static Integer nodePort = NodeConfig.getInstance().getNodePort();
    private final ExecutorService connectionPool;
    private final ExecutorService multicastReceiverPool;

    public Node() {
        this.connectionPool = Executors.newCachedThreadPool();
        this.multicastReceiverPool = Executors.newCachedThreadPool();
    }

    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Node listening on port " + port);

            // Start listening for incoming connections
            connectionPool.submit(this::listenForConnections);

            // Start the multicast receiver in a separate thread
            //multicastReceiverPool.submit(new SensorMulticastReceiver());
            multicastReceiverPool.submit(this::listenForMulticast);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void listenForMulticast() {
        SensorMulticastReceiver multicastReceiver = new SensorMulticastReceiver();
        multicastReceiver.run(); // Start the receiver in the current thread
    }
    public void listenForConnections() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from Node on port " + clientSocket.getPort());
                IncomingConnectionHandler connectionHandler = new IncomingConnectionHandler(clientSocket);
                connectionPool.submit(connectionHandler::handleConnection);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void connectToBootstrapServer(String request, String host, int port) {
        OutgoingConnectionHandler handler = new OutgoingConnectionHandler();
        handler.connectToBootstrapServer(request, host, port);
    }

    public static void main(String[] args) {
        Node node = new Node();
        if (args.length != 1) {
            System.out.println("Usage: java Node <port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        nodeConfig.setNodePort(port);
        System.out.println("Listening on port " + port);
        node.startServer(port);
        node.connectToBootstrapServer("register", "localhost", 9090);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("Enter command (search data <data Id> or connect server <port> or store data <Id> <Value>): ");
                String command = reader.readLine().trim();

                if (command.startsWith("refresh routing")) {
                    node.connectToBootstrapServer("refresh routing", "localhost", 9090);
                } else if (command.startsWith("show routing")) {
                    if (routingTable.isEmpty()) {
                        System.out.println("Routing table is empty");
                    } else {
                        for (Map.Entry<String, RoutingPacket> entry : routingTable.entrySet()) {
                            System.out.println(entry.getKey());
                            System.out.println(entry.getValue().getPort());
                            System.out.println(entry.getValue().getMulticastId());
                        }
                    }
                } else if (command.startsWith("find data")) {
                    String[] tokens = command.split(" ");
                    if (tokens.length != 3) {
                        System.out.println("Invalid command format. Expected: find data <dataId>");
                        continue;
                    }
                    String dataId = tokens[2];
                    DataFinder dataFinder = new DataFinder();
                     dataFinder.findCompositeData(dataId);
                } else if (command.startsWith("store data")) {
                    String[] tokens = command.split(" ");
                    if (tokens.length != 4) {
                        System.out.println("Invalid command format. Expected: store data <dataId> <dataValue>");
                        continue;
                    }
                    String dataId = tokens[2];
                    String dataValue = tokens[3];
                    dataTable.put(dataId, dataValue);
                    System.out.println("Data id " + dataId + " stored successfully");
                } else {
                    System.out.println("Unknown command. Available commands: refresh routing, show routing, find data, store data");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
