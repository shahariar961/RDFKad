package org.rdfkad;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class ConsensusLogAnalyzer {
    private static final Pattern CONSENSUS_PATTERN = Pattern.compile("Consensus Success p(\\d+)");
    private static final Pattern LATENCY_PATTERN = Pattern.compile("Latency  -------------------------------------------------------------([\\d]+) ms");

    public static void main(String[] args) {
        String logFilePath = "nodes.log";  // Assuming 'nodes.log' is in the root of your project directory
        Map<Integer, List<Integer>> latencyMap = new HashMap<>();

        // Initialize the map for tiers p1 to p4
        for (int i = 1; i <= 4; i++) {
            latencyMap.put(i, new ArrayList<>());
        }

        try (BufferedReader br = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            Integer currentTier = null;

            while ((line = br.readLine()) != null) {
                // Ignore lines that are not related to consensus or latency
                if (line.contains("[INFO]") || line.contains("[WARNING]") || line.trim().isEmpty()) {
                    continue;
                }

                Matcher consensusMatcher = CONSENSUS_PATTERN.matcher(line);
                Matcher latencyMatcher = LATENCY_PATTERN.matcher(line);

                if (consensusMatcher.find()) {
                    currentTier = Integer.parseInt(consensusMatcher.group(1));
                } else if (latencyMatcher.find() && currentTier != null) {
                    int latency = Integer.parseInt(latencyMatcher.group(1));
                    latencyMap.get(currentTier).add(latency);
                    currentTier = null;  // Reset after adding latency
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calculate and print the average latency for each tier
        for (int i = 1; i <= 4; i++) {
            List<Integer> latencies = latencyMap.get(i);
            double averageLatency = latencies.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);

            // Round down to one decimal place
            double roundedLatency = Math.floor(averageLatency * 10) / 10.0;
            System.out.println("Tier p" + i + ": Count = " + latencies.size() + ", Average Latency = " + roundedLatency + " ms");
        }
    }
}
