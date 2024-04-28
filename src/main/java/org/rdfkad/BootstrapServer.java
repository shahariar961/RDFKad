package org.rdfkad;

import org.rdfkad.multicast.SensorMulticastSender;
import org.rdfkad.multicast.SensorMulticastServer;
import org.rdfkad.packets.RoutingPacket;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.*;
import java.io.IOException;

import org.rdfkad.handlers.IncomingConnectionHandler;
import org.rdfkad.tables.RoutingTable;


public class BootstrapServer {
    private static ConcurrentHashMap<String, RoutingPacket> routingTable= RoutingTable.getInstance().getMap();
    private static final int BIT_SPACE = 12;
    private static int Bit=2;

    public BootstrapServer() {

    }

    public void start(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Bootstrap Server listening on port " + port);
            SensorMulticastServer multicastServer = new SensorMulticastServer();
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                // Assuming messageSender takes no arguments and is a static method
                SensorMulticastSender.messageSender(); // Adjust according to actual method signature
            }, 0, 10, TimeUnit.SECONDS);
            ExecutorService executorService = Executors.newCachedThreadPool();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                IncomingConnectionHandler connectionHandler = new IncomingConnectionHandler(clientSocket);
                executorService.submit(connectionHandler::handleConnectionBootstrap);

                executorService.submit(multicastServer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        BootstrapServer bootstrapServer = new BootstrapServer();
        bootstrapServer.start(9090);
    }
}