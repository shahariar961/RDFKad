package org.rdfkad.tables;

import org.rdfkad.packets.RoutingPacket;

public class AlarmMatrixObject{
    private boolean alarmState;
    private int multicastId;
    private String dataAddress;

    public AlarmMatrixObject(boolean alarmState, int multicastId, String dataAddress) {
        this.alarmState = alarmState;
        this.multicastId = multicastId;
        this.dataAddress = dataAddress;
    }

    public boolean isAlarmState() {
        return alarmState;
    }

    public void setAlarmState(boolean alarmState) {
        this.alarmState = alarmState;
    }

    public int getMulticastId() {
        return multicastId;
    }

    public void setMulticastId(int multicastId) {
        this.multicastId = multicastId;
    }

    public String getDataAddress() {
        return dataAddress;
    }

    public void setDataAddress(String dataAddress) {
        this.dataAddress = dataAddress;
    }
}
