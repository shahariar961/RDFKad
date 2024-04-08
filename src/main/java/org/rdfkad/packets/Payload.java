package org.rdfkad.packets;

import java.io.Serializable;

public class Payload implements Serializable {
    public String dataId;
    public String nodeId;
    public Integer port;
    String dataValue;

    public String request;
    public boolean flag;


    public Payload(String request,String nodeId,Integer port, String dataId,String dataValue,boolean flag){
        this.request=request;
        this.nodeId=nodeId;
        this.port = port;
        this.dataId=dataId;
        this.dataValue=dataValue;
        this.flag=flag;
    }
    public Payload(String request,String nodeId,Integer port, String dataId,boolean flag){
        this.request=request;
        this.nodeId=nodeId;
        this.port = port;
        this.dataId=dataId;
        this.flag=flag;
    }
    public Payload(String request,String nodeId,Integer port,boolean flag){
        this.request=request;
        this.nodeId=nodeId;
        this.port = port;
        this.flag=flag;
    }
}

