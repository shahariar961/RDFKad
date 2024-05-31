package org.rdfkad.packets;

import java.io.Serializable;
import java.net.InetAddress;

public class RoutingPacket implements Serializable {
    private InetAddress address;
    private Integer port;
    private Integer multicastId;

    public RoutingPacket(InetAddress address, Integer port) {
        this.address = address;
        this.port = port;
    }

    public RoutingPacket(InetAddress address, Integer port, Integer multicastId) {
        this.address = address;
        this.port = port;
        this.multicastId = multicastId;
    }

    public InetAddress getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getMulticastId() {
        return multicastId;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setMulticastId(Integer multicastId) {
        this.multicastId = multicastId;
    }
}
