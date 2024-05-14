package org.rdfkad.packets;

import java.io.Serializable;

public class SensorDataPayload  implements Serializable {
    private int uniqueId;  // Unique identifier for the payload
    private RDFPacket rdfData;
    private String request;// RDF data as a string

    // Constructor
    public SensorDataPayload( int uniqueId, RDFPacket rdfData, String request) {
        this.uniqueId = uniqueId;
        this.rdfData = rdfData;
        this.request=request;
    }

    // Getters and setters
    public int getUniqueId() {
        return uniqueId;
    }


    public RDFPacket getRdfData() {
        return rdfData;
    }


    public String getRequest() {
        return request;
    }
}