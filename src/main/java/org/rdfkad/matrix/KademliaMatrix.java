package org.rdfkad.matrix;

import org.rdfkad.packets.RoutingPacket;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class KademliaMatrix {
    private boolean[][] alarmStates = new boolean[6][6];
    private int[][] multicastIds = new int[6][6];

    public KademliaMatrix() {
        int id = 1;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                multicastIds[i][j] = id++;
            }
        }
    }

    public void activateAlarmById(int multicastId) {
        int[] position = findPositionById(multicastId);
        if (position != null) {
            activateAlarm(position[0], position[1]);
        } else {
            System.out.println("No node with multicast ID " + multicastId);
        }
    }

    public void deactivateAlarmById(int multicastId) {
        int[] position = findPositionById(multicastId);
        if (position != null) {
            deactivateAlarm(position[0], position[1]);
        } else {
            System.out.println("No node with multicast ID " + multicastId);
        }
    }

    private void activateAlarm(int row, int col) {
        if (isValidPosition(row, col)) {
            alarmStates[row][col] = true;
            checkNeighborsAndTakeAction(row, col);
        }
    }

    private void deactivateAlarm(int row, int col) {
        if (isValidPosition(row, col)) {
            alarmStates[row][col] = false;
        }
    }

    private int[] findPositionById(int multicastId) {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (multicastIds[i][j] == multicastId) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    private void checkNeighborsAndTakeAction(int row, int col) {
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            if (isValidPosition(newRow, newCol) && alarmStates[newRow][newCol]) {
                takeAction(row, col, newRow, newCol);
            }
        }
    }

    private void takeAction(int srcRow, int srcCol, int targetRow, int targetCol) {
        System.out.println("Alarm action taken at [" + srcRow + "," + srcCol + "] due to alarm at [" + targetRow + "," + targetCol + "]");
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 6 && col >= 0 && col < 6;
    }

    public static void main(String[] args) {
        KademliaMatrix matrix = new KademliaMatrix();
        matrix.activateAlarmById(34); // Activate alarm at multicast ID 34
    }
    public boolean getAlarmState(int row, int col) {
        if (isValidPosition(row, col)) {
            return alarmStates[row][col];
        }
        return false; // Or throw an exception if preferred
    }
}
