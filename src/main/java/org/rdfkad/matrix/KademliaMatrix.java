package org.rdfkad.matrix;

import org.rdfkad.tables.AlarmMatrixObject;
import org.rdfkad.tables.NodeConfig;

import java.util.ArrayList;
import java.util.List;

public class KademliaMatrix {
    private static final int SIZE = 6;
    private static AlarmMatrixObject[][] alarmStates = new AlarmMatrixObject[SIZE][SIZE];
    private static int[][] multicastIds = new int[SIZE][SIZE];
    private static KademliaMatrix instance = null;
    private NodeConfig nodeConfig = NodeConfig.getInstance();
    private static int ownMulticastId;

    private KademliaMatrix() {
        int id = 1;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                multicastIds[i][j] = id++;
                alarmStates[i][j] = new AlarmMatrixObject(false, multicastIds[i][j], null);
            }
        }

    }

    public static KademliaMatrix getInstance() {
        if (instance == null) {
            instance = new KademliaMatrix();
        }
        return instance;
    }

    public void activateAlarmById(int multicastId, String dataAddress) {
        System.out.println(dataAddress);
        int[] position = findPositionById(multicastId);
        if (position != null) {
            activateAlarm(position[0], position[1], dataAddress);
        } else {
            System.out.println("No node with multicast ID " + multicastId);
        }
    }

    private void activateAlarm(int row, int col, String dataAddress) {
        if (isValidPosition(row, col)) {
            System.out.println(dataAddress);
            alarmStates[row][col].setAlarmState(true);
            alarmStates[row][col].setDataAddress(dataAddress);
            System.out.println("Activating alarm on [" + row + "][" + col + "] with data address " + dataAddress);
        }
    }

    public void deactivateAlarmById(int multicastId) {
        int[] position = findPositionById(multicastId);
        if (position != null) {
            deactivateAlarm(position[0], position[1]);
            if (multicastId == ownMulticastId) {
                nodeConfig.setCurrentAlarmTier(0);
            }
        } else {
            System.out.println("No node with multicast ID " + multicastId);
        }
    }

    private void deactivateAlarm(int row, int col) {
        if (isValidPosition(row, col)) {
            alarmStates[row][col].setAlarmState(false);
            System.out.println("Deactivating alarm on [" + row + "][" + col + "]");
        }
    }

    public int[] findPositionById(int multicastId) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (multicastIds[i][j] == multicastId) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    public List<AlarmMatrixObject> checkNeighborsAndTakeAction() {
        ownMulticastId= nodeConfig.getMulticastId();
        int[] position = findPositionById(ownMulticastId);
        List<AlarmMatrixObject> alarmingNeighbors = new ArrayList<>();
        if (position != null) {
            int row = position[0];
            int col = position[1];
            int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] dir : directions) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];
                if (isValidPosition(newRow, newCol)) {
                    AlarmMatrixObject neighbor = alarmStates[newRow][newCol];
                    System.out.println("Checking neighbor at [" + newRow + "][" + newCol + "]: " +
                            "alarmState=" + neighbor.isAlarmState() +
                            ", multicastId=" + neighbor.getMulticastId() +
                            ", dataAddress=" + neighbor.getDataAddress());
                    if (neighbor.isAlarmState()) {
                        alarmingNeighbors.add(neighbor);
                        System.out.println("Adding Neighbour");
                    }
                }
            }
        } else {
            System.out.println("Own multicast ID position not found in matrix");
        }
        System.out.println("Alarming neighbors found: " + alarmingNeighbors.size());
        return alarmingNeighbors;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    public boolean getAlarmState(int row, int col) {
        if (isValidPosition(row, col)) {
            return alarmStates[row][col].isAlarmState();
        }
        return false; // Or throw an exception if preferred
    }

    public void setOwnMulticastId(int multicastId) {
        ownMulticastId = multicastId;
    }
}
