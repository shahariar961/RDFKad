package org.rdfkad.packets;

public class RDFDataPacket {
    public String subject;
    public String predicate;
    public String Object;


    public RDFDataPacket(String packet) {

        String[] components = packet.split("\\|");
        this.subject = components[0];
        this.predicate = components[1];
        this.Object = components[2];

    }

}

/**
 * Constructs an RDFDataPacket from a serialized RDF string.
 * The serialized RDF is expected to be in the format "subject|predicate|object".
 *
 * @param serializedRDF The serialized RDF string.
 */
//    public RDFDataPacket(RDFDataPacket serializedRDF) {
//        // Splitting the serialized RDF string into components
//        String[] components = serializedRDF.split("\\|");
//
//        // Check if the serialized RDF string has exactly three components
//        if (components.length != 3) {
//            throw new IllegalArgumentException("Invalid serialized RDF format");
//        }
//
//        this.subject = components[0];
////        this.predicate = components[1];
////        this.Object = components[2];
//    }
//    public String serialize() {
//        return subject + "|" + predicate + "|" + Object;
//    }
//}
