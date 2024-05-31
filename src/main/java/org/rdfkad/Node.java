package org.rdfkad;

import org.rdfkad.handlers.DataFinder;
import org.rdfkad.handlers.IncomingConnectionHandler;
import org.rdfkad.handlers.OutgoingConnectionHandler;
import org.rdfkad.multicast.SensorMulticastReceiver;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.tables.DataTable;
import org.rdfkad.tables.NodeConfig;
import org.rdfkad.tables.RoutingTable;

import java.io.IOException;
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

    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
            System.out.println("Node listening on " + ":" + port);

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
            node.startServer(port);
            node.connectToBootstrapServer("register");

            // Perform any predefined actions here instead of waiting for user input
            node.connectToBootstrapServer("refresh routing");

            // Example of predefined actions:
            // node.connectToBootstrapServer("find data", "someDataId");
            // node.connectToBootstrapServer("store data", "someDataId", "someDataValue");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
