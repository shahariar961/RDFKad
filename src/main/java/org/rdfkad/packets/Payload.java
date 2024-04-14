package org.rdfkad.packets;

import java.io.Serializable;

public class Payload implements Serializable {
    public String dataId;
    public String nodeId;
    public Integer port;
    String dataValue;

    public String request;



    public Payload(String request,String nodeId,Integer port, String dataId,String dataValue){
        this.request=request;
        this.nodeId=nodeId;
        this.port = port;
        this.dataId=dataId;
        this.dataValue=dataValue;

    }
    public Payload(String request,String nodeId,Integer port, String dataId){
        this.request=request;
        this.nodeId=nodeId;
        this.port = port;
        this.dataId=dataId;

    }
    public Payload(String request,String nodeId,Integer port){
        this.request=request;
        this.nodeId=nodeId;
        this.port = port;

    }
}

