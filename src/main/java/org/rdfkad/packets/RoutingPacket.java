package org.rdfkad.packets;


import java.io.Serializable;
public class RoutingPacket implements Serializable{
    public Integer port;
    public boolean flag;
    public Integer distance;
    public RoutingPacket(Integer port){
        this.port = port;
            }
    public RoutingPacket(Integer port,Integer distance){
        this.port = port;
        this.distance=distance;
    }
}
