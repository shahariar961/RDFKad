package org.rdfkad.packets;

import java.io.Serializable;

public class RDFPacket implements Serializable {


    public String subject;
    public String predicate;
    public String object;


    public RDFPacket(String subject, String predicate, String object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;

    }
    public String getSubject() {
        return subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getObject() {
        return object;
    }

}
