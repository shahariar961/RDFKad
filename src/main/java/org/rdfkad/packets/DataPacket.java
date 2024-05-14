package org.rdfkad.packets;


public class DataPacket {
    private RDFPacket rdfData;
    private String storageLocation;

    public DataPacket(RDFPacket rdfData, String storageLocation) {
        this.rdfData = rdfData;
        this.storageLocation = storageLocation;
    }

    public RDFPacket getRdfData() {
        return rdfData;
    }

    public void setRdfData(RDFPacket rdfData) {
        this.rdfData = rdfData;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    @Override
    public String toString() {
        return "DataPacket{" +
                "rdfData=" + rdfData +
                ", storageLocation='" + storageLocation + '\'' +
                '}';
    }
}
