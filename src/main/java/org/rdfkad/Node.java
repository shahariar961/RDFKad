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
import java.net.InetAddress;
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
    private final ExecutorService connectionPool;
    private final ExecutorService multicastReceiverPool;

    private static final String BOOTSTRAP_SERVER_IP = "host.docker.internal";
    private static final int BOOTSTRAP_SERVER_PORT = 9090;

    public Node() {
        this.connectionPool = Executors.newCachedThreadPool();
        this.multicastReceiverPool = Executors.newCachedThreadPool();
    }

    public void startServer(InetAddress address, int port) {
        try {
            serverSocket = new ServerSocket(port, 50, address);
            System.out.println("Node listening on " + address.getHostAddress() + ":" + port);

            // Start listening for incoming connections
            connectionPool.submit(this::listenForConnections);

            // Start the multicast receiver in a separate thread
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

    public void connectToBootstrapServer(String request) {
        long startTime = System.currentTimeMillis(); // Start time for latency measurement
        OutgoingConnectionHandler handler = new OutgoingConnectionHandler();
        handler.connectToBootstrapServer(request, BOOTSTRAP_SERVER_IP, BOOTSTRAP_SERVER_PORT);
        long endTime = System.currentTimeMillis(); // End time for latency measurement
        System.out.println("Latency to connect to bootstrap server: " + (endTime - startTime) + " ms");
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Node <address> <port>");
            System.exit(1);
        }

        String address = args[0];
        int port = Integer.parseInt(args[1]);

        Node node = new Node();
        nodeConfig.setNodePort(port);
        System.out.println("Starting Node on " + address + ":" + port);

        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            node.startServer(inetAddress, port);
            node.connectToBootstrapServer("register");
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("Enter command (search data <data Id> or connect server <port> or store data <Id> <Value>): ");
                String command = reader.readLine().trim();

                if (command.startsWith("refresh routing")) {
                    node.connectToBootstrapServer("refresh routing");
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
