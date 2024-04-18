package org.rdfkad.functions;

import java.math.BigInteger;



public class XOR {
    public static BigInteger Distance(String nodeId1, String nodeId2) {
        BigInteger bigInt1 = new BigInteger(nodeId1, 2);
        BigInteger bigInt2 = new BigInteger(nodeId2, 2);
        return bigInt1.xor(bigInt2);
    }
}