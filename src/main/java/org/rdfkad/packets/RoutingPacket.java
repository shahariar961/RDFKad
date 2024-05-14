package org.rdfkad.packets;


import java.io.Serializable;
public class RoutingPacket implements Serializable{
    public Integer port;
    public  String host = "localhost";
    public   Integer multicastId ;
    public RoutingPacket(Integer port){
        this.port = port;
            }
    public RoutingPacket(Integer port, String host){
        this.port = port;
        this.host = host;

    }

    public RoutingPacket(Integer port, String host, Integer multicastId){
        this.port = port;
        this.host = host;
        this.multicastId= multicastId;

    }

    public  Integer getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public Integer getMulticastId() {
        return multicastId;
    }
}
