package org.rdfkad.functions;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Set;


//NEEDS TO BE REDONE
public class IDGenerator {




    /**
     * Generates a 12-bit binary ID with an increasing XOR distance from the given nodeId
     * if all single-bit flipped IDs are taken.
     *
     * @param nodeId The original 12-bit binary ID as a string.
     * @return A new 12-bit binary ID as a string.
     */
    public static String generateID(String nodeId , Hashtable<String, Object> dataTable) {
        // Check if nodeId is a valid 12-bit binary string

        if (nodeId.length() != 12 || !nodeId.matches("[01]{12}")) {
            throw new IllegalArgumentException("Invalid nodeId. It must be a 12-bit binary string.");
        }
        if (dataTable.isEmpty()) {
            return flipBits(nodeId, new HashSet<>()); // flipping no bits, essentially returning the nodeId
        }

        Set<Integer> triedFlips = new HashSet<>();
        String newId;
        boolean idExists;
        int xorDistance = 1;

        do {
            int bitToFlip;
            do {
                bitToFlip = (int) (Math.random() * 12);
            } while (triedFlips.contains(bitToFlip));

            triedFlips.add(bitToFlip);
            newId = flipBits(nodeId, triedFlips);

            idExists = dataTable.containsKey(newId);

            // Check if we have tried all single-bit flips
            if (triedFlips.size() == 12 && idExists) {
                // Increase XOR distance and reset tried flips
                xorDistance++;
                triedFlips.clear();
                if (xorDistance > 12) {
                    throw new IllegalStateException("Unable to generate a unique ID.");
                }
            }
        } while (idExists);

        return newId;
    }

    /**
     * Flips the bits at the specified positions in the nodeId.
     *
     * @param nodeId The original nodeId.
     * @param positions The positions of the bits to flip.
     * @return The nodeId with flipped bits.
     */
    private static String flipBits(String nodeId, Set<Integer> positions) {
        char[] nodeIdChars = nodeId.toCharArray();
        for (Integer position : positions) {
            nodeIdChars[position] = (nodeIdChars[position] == '0') ? '1' : '0';
        }
        return new String(nodeIdChars);
    }

    // Example usage
//    public static void main(String[] args) {
//        IDGenerator generator = new IDGenerator();
//
//        // Example: Populating the dataTable with some IDs
//        generator.dataTable.put("110010101011", "SomeValue");
//        generator.dataTable.put("110010101010", "AnotherValue");
//
//        String nodeId = "110010101011"; // Example 12-bit nodeId
//        String newId = generator.generateID(nodeId);
//
//        System.out.println("Original Node ID: " + nodeId);
//        System.out.println("Generated Node ID: " + newId);
//    }
}
