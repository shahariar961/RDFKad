//package org.rdfkad.unittests;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.rdfkad.handlers.RDFStore;
//import org.rdfkad.handlers.RDFStore.RDFPacket;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.Hashtable;
//
//import static org.junit.Assert.*;
//
//public class RDFStoreTest {
//    private RDFStore rdfStore;
//    private Hashtable<String, Object> hashTable;
//
//    @Before
//    public void setUp() {
//        hashTable = new Hashtable<>();
//        rdfStore = new RDFStore(hashTable);
//    }
//
//    @Test
//    public void testStoreRDF() {
//        RDFPacket rdfPacket = new RDFPacket("subject", "predicate", "object");
//        rdfStore.storeRDF(rdfPacket);
//
//        // Print from hashTable
//        hashTable.forEach((key, value) -> System.out.println("Key: " + key + ", Value: " + value));
//
//        // Print from rdf_storage.txt
////        try (BufferedReader reader = new BufferedReader(new FileReader("rdf_storage.txt"))) {
////            String line;
////            while ((line = reader.readLine()) != null) {
////                System.out.println(line);
////            }
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//    }
//}