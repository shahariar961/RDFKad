    package org.rdfkad;

    import org.rdfkad.functions.XOR;
    import org.rdfkad.packets.RoutingPacket;
    import org.rdfkad.tables.NodeConfig;
    import org.rdfkad.tables.RoutingTable;

    import java.math.BigInteger;
    import java.util.*;
    import java.util.concurrent.ConcurrentHashMap;

    public class Bucket {

        // Singleton instance
        private static Bucket instance;

        // Private mutable list of buckets
        private final List<Set<String>> buckets;

        private  final  String ownNodeId ;
        private final ConcurrentHashMap<String, RoutingPacket> routingTable;

        private Bucket(int bucketCount) {
            this.routingTable = RoutingTable.getInstance().getMap();
            this.buckets = new ArrayList<>(bucketCount);
            ownNodeId = NodeConfig.getInstance().getNodeId();
            for (int i = 0; i < bucketCount; i++) {
                this.buckets.add(new HashSet<>());
            }
        }

        // Method to get the singleton instance
        public static synchronized Bucket getInstance() {
            if (instance == null) {
                instance = new Bucket(13);
            }
            return instance;
        }

        // Public method to get read-only access to the buckets
        public List<Set<String>> getBuckets() {
            List<Set<String>> unmodifiableBuckets = new ArrayList<>();
            for (Set<String> bucket : buckets) {
                unmodifiableBuckets.add(Collections.unmodifiableSet(bucket));
            }
            return Collections.unmodifiableList(unmodifiableBuckets);
        }

        // Method to add a node to a specific bucket by index
        public synchronized void addNodeToBucket(int index, String nodeId) {
            if (index >= 0 && index < buckets.size()) {
                buckets.get(index).add(nodeId);
            } else {
                throw new IndexOutOfBoundsException("Bucket index out of range: " + index);
            }
        }

        // Method to sort nodes into buckets based on leading zeros
        public void sortIntoBuckets() {
            for (String otherNodeId : routingTable.keySet()) {
                BigInteger distance = XOR.Distance(ownNodeId, otherNodeId);
                int bucketIndex = getLeadingZeros(distance);
                addNodeToBucket(bucketIndex, otherNodeId);
            }
        }

        // Adjusted to a 12-bit identifier space
        public static int getLeadingZeros(BigInteger distance) {
            int highestSetBit = distance.bitLength() - 1;
            return 11 - highestSetBit;
        }
    }
