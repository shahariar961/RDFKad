package org.rdfkad.unittests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rdfkad.matrix.KademliaMatrix;
import org.rdfkad.packets.RoutingPacket;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class KademliaMatrixTest {
    private KademliaMatrix matrix;
    private Random random;

    @BeforeEach
    public void setUp() {
        matrix = KademliaMatrix.getInstance();
        random = new Random();
    }

    @Test
    public void testChangeAndAccessAlarmStates() throws UnknownHostException {
        // Activate alarm at multicast ID 34
        matrix.activateAlarmById(34, generateRandomDataAddress());
        // Check if the alarm state is true at the position corresponding to ID 34
        int[] position = matrix.findPositionById(34);
        assertTrue(matrix.getAlarmState(position[0], position[1]));

        printAlarmStates();
        // Deactivate alarm at multicast ID 34
        matrix.deactivateAlarmById(34);
        // Check if the alarm state is false at the position corresponding to ID 34
        assertFalse(matrix.getAlarmState(position[0], position[1]));

        // Print the entire alarmStates array
        System.out.println("------------------------");
        printAlarmStates();
    }

    @Test
    public void testCheckNeighborsAndTakeAction() throws UnknownHostException {
        // Set own multicast ID to 1
        matrix.activateAlarmById(1,generateRandomDataAddress());
        matrix.activateAlarmById(2,  generateRandomDataAddress());
        // Set ownMulticastId to 1
        matrix.setOwnMulticastId(1);
        // Check neighbors and take action
        matrix.checkNeighborsAndTakeAction();
    }

    private void printAlarmStates() {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                System.out.print(matrix.getAlarmState(i, j) ? "O " : "X ");
            }
            System.out.println();
        }
    }

    private RoutingPacket generateRandomRoutingPacket() throws UnknownHostException {
        Integer nodeId = random.nextInt(100);
        String port = String.valueOf(5000 + random.nextInt(1000)); // Random port between 5000 and 6000
        Integer address = random.nextInt(256);
        return new RoutingPacket(nodeId, port, address);
    }

    private String generateRandomDataAddress() {
        return "DataAddress" + random.nextInt(100);
    }
}