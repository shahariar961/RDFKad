package org.rdfkad.unittests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rdfkad.matrix.KademliaMatrix;

import static org.junit.jupiter.api.Assertions.*;

public class KademliaMatrixTest {
    private KademliaMatrix matrix;

    @BeforeEach
    public void setUp() {
        matrix = new KademliaMatrix();
    }

    @Test
    public void testActivateAlarm() {
        matrix.activateAlarmById(34);
        assertTrue(matrix.getAlarmState(5, 4), "Alarm should be activated at position corresponding to ID 34");
    }

    @Test
    public void testDeactivateAlarm() {
        matrix.activateAlarmById(34);
        matrix.deactivateAlarmById(34);
        assertFalse(matrix.getAlarmState(5, 4), "Alarm should be deactivated at position corresponding to ID 34");
    }

    @Test
    public void testCheckNeighborAlarm() {
        matrix.activateAlarmById(34); // Activate at (5,4)
        matrix.activateAlarmById(33); // Activate at (5,3)
        assertTrue(matrix.getAlarmState(5, 3), "Alarm at (5,3) should be activated");
        assertTrue(matrix.getAlarmState(5, 4), "Alarm at (5,4) should be activated");
    }

    @Test
    public void testPrintMatrix() {
        matrix.activateAlarmById(1);
        matrix.activateAlarmById(36);
        printMatrix();
    }

    private void printMatrix() {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (matrix.getAlarmState(i, j)) {
                    System.out.print("O ");
                } else {
                    System.out.print("X ");
                }
            }
            System.out.println();
        }
    }
}

