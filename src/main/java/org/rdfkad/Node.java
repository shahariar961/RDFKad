package org.rdfkad;

import org.rdfkad.handlers.IncomingConnectionHandler;
import org.rdfkad.handlers.OutgoingConnectionHandler;
import org.rdfkad.handlers.hash;
import org.rdfkad.functions.XOR;
import org.rdfkad.packets.HashPacket;
import org.rdfkad.packets.RDFDataPacket;
import org.rdfkad.packets.RoutingPacket;

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
    private String nodeId="null";
    private static Integer nodePort;
    private HashMap<String, RoutingPacket> routingTableMap;

    private Hashtable<String,Object> dataTable;
    private HashMap<String,RoutingPacket> Bucket;

    private Map<String, Object> overlayTable = new HashMap<>();

    public BlockingQueue<List<String>> getDataQueue() {
        return dataQueue;
    }

    public Node() {
        this.dataQueue = new ArrayBlockingQueue<>(10);
        this.dataTable = new Hashtable<>();
        this.Bucket=new HashMap<>();
        this.routingTableMap=new HashMap<>();
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
                System.out.println("Accepted connection from Node on port " + clientSocket.getPort());
                IncomingConnectionHandler connectionHandler = new IncomingConnectionHandler(clientSocket, routingTableMap);
                connectionHandler.handleConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void connectToBootstrapServer(String host, int port) {
        OutgoingConnectionHandler handler = new OutgoingConnectionHandler();
        handler.connectToBootstrapServer(host, port, routingTableMap);
    }

    public static void main(String[] args) {
        Node node = new Node();
        if (args.length != 1) {
            System.out.println("Usage: java Node <port>");
            System.exit(1);
        }
        int nodePort = Integer.parseInt(args[0]);
        node.startServer(nodePort);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("Enter command (search data <data Id> or connect server <port> or store data <Id> <Value>): ");
                String command = reader.readLine().trim();

                if (command.startsWith("connect server")) {
                    node.connectToBootstrapServer("localhost", 9090);
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