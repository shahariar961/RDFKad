package org.rdfkad;

import org.rdfkad.handlers.IncomingConnectionHandler;
import org.rdfkad.handlers.OutgoingConnectionHandler;
import org.rdfkad.multicast.MulticastMessagePrinter;
import org.rdfkad.multicast.SensorMulticastSender;
import org.rdfkad.multicast.SensorMulticastServer;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.tables.RoutingTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BootstrapServer {
    private static ConcurrentHashMap<String, RoutingPacket> routingTable = RoutingTable.getInstance().getMap();
    private static final int BIT_SPACE = 12;
    public ServerSocket serverSocket;
    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final int MULTICAST_PORT = 4446;
    private final ExecutorService connectionPool;
    private static Map<Integer, Long> sendTimestamps = new HashMap<>(); // Map to store timestamps

    public BootstrapServer() {
        this.connectionPool = Executors.newCachedThreadPool();
    }

    public void start(int port) {
        try {
            InetAddress localHostAddress = InetAddress.getLocalHost();
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
            System.out.println("Bootstrap Server listening on port " + port);
            SensorMulticastServer multicastServer = new SensorMulticastServer();

            connectionPool.submit(this::listenForConnections);
            connectionPool.submit(multicastServer);
            connectionPool.submit(new MulticastMessagePrinter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenForConnections() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                IncomingConnectionHandler connectionHandler = new IncomingConnectionHandler(clientSocket);
                // Submit the handler to the executor service
                connectionPool.submit(connectionHandler::handleConnection);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        BootstrapServer bootstrapServer = new BootstrapServer();
        bootstrapServer.start(9090);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("Enter command (send temp data  <multicast id>  <value> ): ");
                String command = reader.readLine().trim();

                if (command.startsWith("send data ")) {
                    String[] tokens = command.split(" ");
                    if (tokens.length != 4) {
                        System.out.println("Invalid command format. Expected: send data <multicastId> <temperature>");
                        continue;
                    }
                    try {
                        int multicastId = Integer.parseInt(tokens[2]);
                        int temperature = Integer.parseInt(tokens[3]);

                        // Store the send timestamp
                        sendTimestamps.put(multicastId, System.currentTimeMillis());

                        // Send the payload using the multicast server
                        SensorMulticastSender.sensorDataMessageSender(multicastId, temperature, "sensor info");

                        System.out.println("Sent data to multicast ID " + multicastId);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid multicast ID. Please provide a numeric value.");
                    }
                } else if (command.startsWith("show routing")) {
                    if (routingTable.isEmpty()) {
                        System.out.println("Routing table is empty");
                    } else {
                        for (Map.Entry<String, RoutingPacket> entry : routingTable.entrySet()) {
                            System.out.println(entry.getKey());
                            System.out.println(entry.getValue().getPort());
                            System.out.println(entry.getValue().getAddress().getHostAddress());
                        }
                    }
                } else if (command.startsWith("update routing")) {
                    OutgoingConnectionHandler handler = new OutgoingConnectionHandler();
                    for (Map.Entry<String, RoutingPacket> entry : routingTable.entrySet()) {
                        String nodeId = entry.getKey();
                        RoutingPacket routingPacket = entry.getValue();
                        InetAddress address = routingPacket.getAddress();
                        int port = routingPacket.getPort();

                        System.out.println("Connecting to Node ID: " + nodeId);

                        handler.connectToBootstrapServer("update routing", address.getHostAddress(), port);
                    }

                } else if (command.startsWith("time")) {
                    List<Long> latencies = MulticastMessagePrinter.getLatencies();
                    if (latencies.isEmpty()) {
                        System.out.println("No latencies recorded yet.");
                    } else {
                        for (Long latency : latencies) {
                            System.out.println("Latency: " + latency + " ms");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static long getSendTimestamp(int multicastId) {
        return sendTimestamps.getOrDefault(multicastId, -1L);
    }
}
