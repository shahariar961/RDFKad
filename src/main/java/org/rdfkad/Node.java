package org.rdfkad;

import org.rdfkad.handlers.IncomingConnectionHandler;
import org.rdfkad.handlers.OutgoingConnectionHandler;
import org.rdfkad.multicast.SensorMulticastReceiver;
import org.rdfkad.packets.RoutingPacket;
import org.rdfkad.tables.DataTable;
import org.rdfkad.tables.RoutingTable;

import java.util.concurrent.ConcurrentHashMap;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Node {
    private BlockingQueue<List<String>> dataQueue;
    private ServerSocket serverSocket;
    private static ConcurrentHashMap<String, RoutingPacket> routingTable = RoutingTable.getInstance().getMap();
    private ConcurrentHashMap<String, Object> dataTable = DataTable.getInstance().getMap();
    private static String nodeId="null";

    private static Integer multicastId=0;

    private static Integer nodePort;
    private final List<Set<String>> bucket;


    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Node listening on port " + port);

            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.submit(this::listenForConnections);
            executorService.submit(new SensorMulticastReceiver());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenForConnections() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from Node on port " + clientSocket.getPort());
                IncomingConnectionHandler connectionHandler = new IncomingConnectionHandler(clientSocket);
                connectionHandler.handleConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void connectToBootstrapServer(String request,String host, int port) {
        OutgoingConnectionHandler handler = new OutgoingConnectionHandler(this);
        handler.connectToBootstrapServer(request,host, port);
    }
    public static void main(String[] args) {
        Node node = new Node();
        if (args.length != 1) {
            System.out.println("Usage: java Node <port>");
            System.exit(1);
        }
        nodePort = Integer.parseInt(args[0]);
        node.startServer(nodePort);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                node.connectToBootstrapServer("register","localhost", 9090);
                System.out.print("Enter command (search data <data Id> or connect server <port> or store data <Id> <Value>): ");
                String command = reader.readLine().trim();

                if (command.startsWith("connect server")) {
                    node.connectToBootstrapServer("register","localhost", 9090);
                } else if (command.startsWith("show routing")) {
                    if (routingTable.isEmpty()){
                        System.out.println("Routing table is empty");
                    }else {
                        for (Map.Entry<String,RoutingPacket> entry : routingTable.entrySet()){
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

    public  void  setMulticastId(int multicastId) {
        this.multicastId = multicastId;
    }

    public static String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public static Integer getMulticastId() {
        return multicastId;
    }

    public static Integer getNodePort() {
        return nodePort;
    }
    public Node() {
        this.dataQueue = new ArrayBlockingQueue<>(10);
        this.bucket = new ArrayList<>();
    }
}