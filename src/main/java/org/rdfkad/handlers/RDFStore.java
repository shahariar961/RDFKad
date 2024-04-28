package org.rdfkad.handlers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Hashtable;

public class RDFStore {
    private Hashtable<String, Object> hashTable;
    private final String storageFilePath = "rdf_storage.txt";
    public RDFStore(Hashtable<String, Object> sharedTable) {
        this.hashTable = sharedTable;
    }
    public void storeRDF(RDFPacket rdf) {
        try {
            // Step 1: Store RDF packet in local file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(storageFilePath, true))) {
                writer.write(rdf.toString() + "\n");
            }

            // Step 2: Hash subject, predicate, object and store in hash table
            String subjectHash = get12BitHash(rdf.subject);
            String predicateHash = get12BitHash(rdf.predicate);
            String objectHash = get12BitHash(rdf.object);

            hashTable.put(subjectHash, rdf.subject);
            hashTable.put(predicateHash, rdf.predicate);
            hashTable.put(objectHash, rdf.object);

            // Step 3: Hash the entire packet and store composite hash
            String packetHash = get12BitHash(rdf.toString());
            String compositeValue = String.join(",", subjectHash, predicateHash, objectHash);
            hashTable.put(packetHash, compositeValue);

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
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

    public static class RDFPacket {
        String subject;
        String predicate;
        String object;

        public RDFPacket(String subject, String predicate, String object) {
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
        }

        @Override
        public String toString() {
            return "Subject: " + subject + ", Predicate: " + predicate + ", Object: " + object;
        }
    }

}

