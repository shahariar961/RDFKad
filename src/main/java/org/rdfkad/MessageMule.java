package org.rdfkad;

import org.rdfkad.handlers.IncomingConnectionHandler;
import org.rdfkad.multicast.SensorMulticastSender;
import org.rdfkad.packets.SensorDataPayload;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class MessageMule {
    private static final int SERVER_PORT = 9091;
    private static final int BACKLOG = 100;
    private static final int CORE_POOL_SIZE = 20;
    private static final int MAX_POOL_SIZE = 40;
    private static final long KEEP_ALIVE_TIME = 60L;

    private final ExecutorService connectionPool;
    private final SensorMulticastSender multicastSender;
    private ServerSocket serverSocket;

    public MessageMule() {
        this.connectionPool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        this.multicastSender = new SensorMulticastSender();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT, BACKLOG);
            System.out.println("Message Mule listening on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(30000); // Set socket read timeout to 30 seconds
                connectionPool.submit(() -> handleConnection(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket clientSocket) {
        try (ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream())) {
            SensorDataPayload payload = (SensorDataPayload) inputStream.readObject();
            System.out.println("Received payload: " + payload);
            multicastSender.sensorDataMessageSender(payload.getUniqueId(), payload.getDataAddress(), payload.getRequest());
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error handling connection: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        MessageMule messageMule = new MessageMule();
        messageMule.start();
    }
}
