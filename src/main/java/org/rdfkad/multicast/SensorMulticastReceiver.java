package org.rdfkad.multicast;

import org.rdfkad.handlers.ConsensusHandler;
import org.rdfkad.packets.SensorDataPayload;
import org.rdfkad.packets.RDFPacket;
import org.rdfkad.tables.NodeConfig;
import org.rdfkad.handlers.RDFDataHandler;
import org.rdfkad.matrix.KademliaMatrix;
import org.rdfkad.tables.AlarmMatrixObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SensorMulticastReceiver implements Runnable {
    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final int MULTICAST_PORT = 4446;
    private static final String BOOTSTRAP_SERVER_HOST = "localhost";
    private static final int BOOTSTRAP_SERVER_PORT = 9090;

    private RDFDataHandler rdfDataHandler = new RDFDataHandler();
    private NodeConfig nodeConfig = NodeConfig.getInstance();
    private KademliaMatrix kademliaMatrix = KademliaMatrix.getInstance();
    private ConsensusHandler consensusHandler = new ConsensusHandler();

    private boolean selfAlarmState;
    private int currentAlarmTier;
    private MulticastSocket multicastSocket;
    private final ExecutorService senderPool = Executors.newSingleThreadExecutor();
    private static final Map<Integer, Long> receiveTimestamps = new HashMap<>();

    @Override
    public void run() {
        try {
            multicastSocket = new MulticastSocket(MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            multicastSocket.joinGroup(group);

            while (true) {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                multicastSocket.receive(packet);

                // Deserialize the incoming data
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
                    Object object = objectInputStream.readObject();
                    if (object instanceof SensorDataPayload) {
                        SensorDataPayload payload = (SensorDataPayload) object;
                        int uniqueId = payload.getUniqueId();
                        int ownMulticastId = nodeConfig.getMulticastId();
                        String request = payload.getRequest();

                        if ("sensor info".equals(request)) {
                            // Record the receive timestamp


                            if (ownMulticastId == uniqueId) {
                                System.out.println("Received relevant payload:");
                                System.out.println("Unique ID: " + payload.getUniqueId());
                                RDFPacket rdfData = payload.getRdfData();

                                int temperature = Integer.parseInt(rdfData.getObject());
                                String dataAddress = rdfDataHandler.storeRDF(rdfData);
                                System.out.println(dataAddress);

                                System.out.println("Temperature: " + temperature);
                                if (temperature > 45) {
                                    System.out.println("Above Threshold handling alarm");
                                    senderPool.submit(() -> handleTemperatureAlarm(ownMulticastId, dataAddress, temperature));
                                } else {
                                    handleTemperatureBelowThreshold(ownMulticastId, dataAddress);
                                }
                            }
                        } else if (("alarm p1".equals(request) || "alarm p2".equals(request) || "alarm p3".equals(request)) && uniqueId != ownMulticastId) {
                            String payloadDataAddress = payload.getDataAddress();
                            kademliaMatrix.activateAlarmById(uniqueId, payloadDataAddress);
                            System.out.println("Alarm Status p1 Temp Alarm on Node: " + uniqueId);
                        } else if ("alarm off".equals(request)) {
                            kademliaMatrix.deactivateAlarmById(uniqueId);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Error during deserialization: " + e.getMessage());
                }
            }
        } catch (SocketException e) {
            System.out.println("Socket Exception: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Exception: " + e.getMessage());
        } finally {
            if (multicastSocket != null && !multicastSocket.isClosed()) {
                try {
                    InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
                    multicastSocket.leaveGroup(group);
                    multicastSocket.close();
                } catch (IOException e) {
                    System.out.println("Error leaving multicast group: " + e.getMessage());
                }
            }
            senderPool.shutdown();
        }
    }

    private void handleTemperatureAlarm(int ownMulticastId, String dataAddress, int temperature) {
        List<AlarmMatrixObject> alarmingNeighbors = kademliaMatrix.checkNeighborsAndTakeAction();
        selfAlarmState = nodeConfig.getSelfAlarmState();
        currentAlarmTier = nodeConfig.getCurrentAlarmTier();
        System.out.println(alarmingNeighbors);

        if (!selfAlarmState) {
            if (alarmingNeighbors.isEmpty()) {
                AlarmMatrixObject ownAlarm = new AlarmMatrixObject(true, ownMulticastId, dataAddress);
                alarmingNeighbors.add(ownAlarm);
                System.out.println("No Neighbours Alarming, Setting Alarm Status to P1");
                kademliaMatrix.activateAlarmById(ownMulticastId, dataAddress);
                nodeConfig.setCurrentAlarmTier(1);
                nodeConfig.setSelfAlarmState(true);
                receiveTimestamps.put(ownMulticastId ,System.currentTimeMillis());
                if (consensusHandler.runConsensusAlgorithm(alarmingNeighbors)) {
                    nodeConfig.setCurrentAlarmTier(2);
                    sendUnicastMessage(ownMulticastId, dataAddress, "alarm p1");
                    recordConsensusTimestampAndCalculateLatency(ownMulticastId);
                }
            } else if (alarmingNeighbors.size() == 1) {
                AlarmMatrixObject ownAlarm = new AlarmMatrixObject(true, ownMulticastId, dataAddress);
                System.out.println("One Neighbour is Alarming, Starting P1 Alarm, Seeking Consensus to Upgrade Alarm");
                kademliaMatrix.activateAlarmById(ownMulticastId, dataAddress);
                alarmingNeighbors.add(ownAlarm);
                receiveTimestamps.put(ownMulticastId ,System.currentTimeMillis());
                if (consensusHandler.runConsensusAlgorithm(alarmingNeighbors)) {
                    nodeConfig.setCurrentAlarmTier(2);
                    sendUnicastMessage(ownMulticastId, dataAddress, "alarm p2");
                    nodeConfig.setSelfAlarmState(true);
                    recordConsensusTimestampAndCalculateLatency(ownMulticastId);
                }
            } else if (alarmingNeighbors.size() == 2) {
                AlarmMatrixObject ownAlarm = new AlarmMatrixObject(true, ownMulticastId, dataAddress);
                System.out.println("One Neighbour is Alarming, Starting P1 Alarm, Seeking Consensus to Upgrade Alarm");
                kademliaMatrix.activateAlarmById(ownMulticastId, dataAddress);
                alarmingNeighbors.add(ownAlarm);
                nodeConfig.setSelfAlarmState(true);
                receiveTimestamps.put(ownMulticastId, System.currentTimeMillis());
                if (consensusHandler.runConsensusAlgorithm(alarmingNeighbors)) {
                    nodeConfig.setCurrentAlarmTier(3);
                    sendUnicastMessage(ownMulticastId, dataAddress, "alarm p3");
                    recordConsensusTimestampAndCalculateLatency(ownMulticastId);
                }
            }
            else if (alarmingNeighbors.size() >= 3) {
                AlarmMatrixObject ownAlarm = new AlarmMatrixObject(true, ownMulticastId, dataAddress);
                System.out.println("One Neighbour is Alarming, Starting P1 Alarm, Seeking Consensus to Upgrade Alarm");
                kademliaMatrix.activateAlarmById(ownMulticastId, dataAddress);
                alarmingNeighbors.add(ownAlarm);
                nodeConfig.setSelfAlarmState(true);
                receiveTimestamps.put(ownMulticastId, System.currentTimeMillis());
                if (consensusHandler.runConsensusAlgorithm(alarmingNeighbors)) {
                    nodeConfig.setCurrentAlarmTier(4);
                    sendUnicastMessage(ownMulticastId, dataAddress, "alarm p4");
                    recordConsensusTimestampAndCalculateLatency(ownMulticastId);
                }
            }
        } else {
            System.out.println("Alarm already activated");
        }
    }

    private void handleTemperatureBelowThreshold(int ownMulticastId, String dataAddress) {
        System.out.println("Temperature is below 45 degrees");
        selfAlarmState = nodeConfig.getSelfAlarmState();
        if (selfAlarmState) {
            System.out.println("Alarm Deactivated");
            kademliaMatrix.deactivateAlarmById(ownMulticastId);
            nodeConfig.setSelfAlarmState(false);
            sendUnicastMessage(ownMulticastId, dataAddress, "alarm off");
        }
    }

    private void sendUnicastMessage(int multicastId, String dataAddress, String request) {
        SensorDataPayload payload = new SensorDataPayload(multicastId, dataAddress, request);

        // Serialize the SensorDataPayload object
        try (Socket socket = new Socket(BOOTSTRAP_SERVER_HOST, BOOTSTRAP_SERVER_PORT);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())) {

            outputStream.writeObject(payload);
            System.out.println("Sent unicast message to bootstrap server");
        } catch (IOException e) {
            System.out.println("Error sending unicast message: " + e.getMessage());
        }
    }

    private void recordConsensusTimestampAndCalculateLatency(int uniqueId) {
        // Record the consensus timestamp
        long consensusTimestamp = System.currentTimeMillis();

        // Retrieve the receive timestamp
        Long receiveTimestamp = receiveTimestamps.get(uniqueId);
        if (receiveTimestamp != null) {
            // Calculate and print the latency
            long latency = consensusTimestamp - receiveTimestamp;
            System.out.println("Latency from receiving to consensus for multicast ID " + uniqueId + ": " + latency + " ms");
        } else {
            System.out.println("Receive timestamp not found for multicast ID: " + uniqueId);
        }
    }
}
