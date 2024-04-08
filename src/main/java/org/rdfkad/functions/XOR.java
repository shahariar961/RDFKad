package org.rdfkad.functions;

public class XOR {
    public static int Distance(String id1, String id2) {
        // Convert binary strings to integers
        int num1 = Integer.parseInt(id1, 2);
        int num2 = Integer.parseInt(id2, 2);

        // Perform XOR operation
        int xor = num1 ^ num2;

        // Count the number of set bits in the result
        int xorDistance = Integer.bitCount(xor);

        return xorDistance;
    }

//    public static void main(String[] args) {
//        // Example usage
//        String id1 = "0001"; // binary representation of 10
//        String id2 = "0001"; // binary representation of 12
//
//        int distance = Distance(id1, id2);
//        System.out.println("XOR Distance: " + distance);
//    }
}