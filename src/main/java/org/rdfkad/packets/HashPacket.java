package org.rdfkad.packets;

public class HashPacket {

    public String hashedSubject;
    public String hashedPredicate;
    public String hashedObject;
    public HashPacket(String hashedObject, String hashedPredicate, String hashedSubject){
        this.hashedObject=hashedObject;
        this.hashedPredicate=hashedPredicate;
        this.hashedSubject=hashedSubject;
    }
}
