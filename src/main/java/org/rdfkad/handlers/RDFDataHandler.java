package org.rdfkad.handlers;

import org.rdfkad.packets.RDFPacket;
import org.rdfkad.tables.DataTable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

public class RDFDataHandler {
    private ConcurrentHashMap<String, Object> dataTable;

    public RDFDataHandler() {
        this.dataTable = DataTable.getInstance().getMap();
    }
    public String storeRDF(RDFPacket rdf) {
        String packetHash = null;
        try {
            // Step 1: Store RDF packet in local file

            // Step 2: Hash subject, predicate, object and store in hash table
            String subjectHash = get12BitHash(rdf.subject);
            String predicateHash = get12BitHash(rdf.predicate);
            String objectHash = get12BitHash(rdf.object);

            dataTable.put(subjectHash, rdf.subject);
            dataTable.put(predicateHash, rdf.predicate);
            dataTable.put(objectHash, rdf.object);
            DataHandler dataHandler = new DataHandler();
            dataHandler.sendData(subjectHash, rdf.subject);
            dataHandler.sendData(predicateHash, rdf.predicate);
            dataHandler.sendData(objectHash, rdf.object);

            // Step 3: Hash the entire packet and store composite hash
            packetHash = get12BitHash(rdf.toString());
            String compositeValue = String.join(",", subjectHash, predicateHash, objectHash);
            dataTable.put(packetHash, compositeValue);
            dataHandler.sendData(packetHash, compositeValue);
            System.out.println("Composite Data ID :" + packetHash);


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return packetHash;
    }

    public String get12BitHash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(input.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();

        BigInteger hash = new BigInteger(1, digest);
        // Reduce to 12 bits
        hash = hash.mod(new BigInteger("4096"));  // 2^12 = 4096
        // Ensure the hash is always 12 bits long
        String binaryString = hash.toString(2);  // Convert to binary string without leading zeros
        return String.format("%12s", binaryString).replace(' ', '0');  // Pad with zeros on the left if necessary
    }






}

