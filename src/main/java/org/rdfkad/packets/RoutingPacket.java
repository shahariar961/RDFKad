package org.rdfkad.packets;


import java.io.Serializable;
public class RoutingPacket implements Serializable{
    public Integer port;
    public boolean flag;
    public Integer distance;
    public RoutingPacket(Integer port, boolean flag){
        this.port = port;
        this.flag = flag;
    }
    public RoutingPacket(Integer port, boolean flag,Integer distance){
        this.port = port;
        this.flag = flag;
        this.distance=distance;
    }
}
