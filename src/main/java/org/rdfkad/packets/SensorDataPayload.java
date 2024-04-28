package org.rdfkad.packets;

import java.io.Serializable;

public class SensorDataPayload  implements Serializable {
    private int uniqueId;  // Unique identifier for the payload
    private RDFPacket rdfData;   // RDF data as a string

    // Constructor
    public SensorDataPayload( int uniqueId, RDFPacket rdfData) {
        this.uniqueId = uniqueId;
        this.rdfData = rdfData;
    }

    // Getters and setters
    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    public RDFPacket getRdfData() {
        return rdfData;
    }

    public void setRdfData(RDFPacket rdfData) {
        this.rdfData = rdfData;
    }
}