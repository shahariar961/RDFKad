package org.rdfkad;

import org.rdfkad.multicast.SensorMulticastSender;
import org.rdfkad.multicast.SensorMulticastServer;
import org.rdfkad.packets.RDFPacket;
import org.rdfkad.packets.RoutingPacket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.io.IOException;

import org.rdfkad.handlers.IncomingConnectionHandler;
import org.rdfkad.packets.SensorDataPayload;
import org.rdfkad.tables.RoutingTable;


public class BootstrapServer {
    private static ConcurrentHashMap<String, RoutingPacket> routingTable= RoutingTable.getInstance().getMap();
    private static final int BIT_SPACE = 12;
    public ServerSocket serverSocket;
    private static int Bit=2;

    public BootstrapServer() {

    }

    public void start(int port) {
        try {
             serverSocket = new ServerSocket(port);
            System.out.println("Bootstrap Server listening on port " + port);
            SensorMulticastServer multicastServer = new SensorMulticastServer();
//            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//            scheduler.scheduleAtFixedRate(() -> {
//                // Assuming messageSender takes no arguments and is a static method
//                SensorMulticastSender.messageSender(); // Adjust according to actual method signature
//            }, 0, 10, TimeUnit.SECONDS);
            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.submit(this::listenForConnections);

            executorService.submit(multicastServer);

    }catch (IOException e) {
        e.printStackTrace();
    }
    }
    public void listenForConnections() {
            while (true) {
                try {
                Socket clientSocket = serverSocket.accept();
                IncomingConnectionHandler connectionHandler = new IncomingConnectionHandler(clientSocket);
                connectionHandler.handleConnectionBootstrap();


        }catch (IOException e) {
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
                        String temperature = tokens[3];

                        // Create the RDFPacket and SensorDataPayload

                        // Send the payload using the multicast server
                        SensorMulticastSender.singularMessageSender(multicastId, temperature,"sensor data");

                        System.out.println("Sent data to multicast ID " + multicastId);

                    } catch (NumberFormatException e) {
                        System.out.println("Invalid multicast ID. Please provide a numeric value.");
                    }

                }
                else if (command.startsWith("show routing")) {
                    if (routingTable.isEmpty()){
                        System.out.println("Routing table is empty");
                    }else {
                        for (Map.Entry<String,RoutingPacket> entry : routingTable.entrySet()){
                            System.out.println(entry.getKey());
                            System.out.println(entry.getValue().getPort());
                        }
                    }
                }else {
                    System.out.println("Unknown command. Available commands: connect node <host> <port> or connect server <host> <port>");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    }
