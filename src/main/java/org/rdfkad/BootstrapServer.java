package org.rdfkad;

import org.rdfkad.packets.RoutingPacket;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.InetAddress;
import java.io.IOException;
import org.rdfkad.datahandlers.ConnectionHandler;



public class BootstrapServer {
    private HashMap<String, RoutingPacket> routingTableMap;
    private static final int BIT_SPACE = 12;
    private static int Bit=2;

    public BootstrapServer() {
        this.routingTableMap = new HashMap<>();
    }

    public void start(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Bootstrap Server listening on port " + port);

            ExecutorService executorService = Executors.newCachedThreadPool();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ConnectionHandler connectionHandler = new ConnectionHandler(clientSocket, routingTableMap);
                executorService.submit(connectionHandler::handleConnection);
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