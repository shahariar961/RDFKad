package org.rdfkad.multicast;

import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.rdfkad.Node;
import org.rdfkad.handlers.RDFDataHandler;
import org.rdfkad.packets.RDFPacket;
import org.rdfkad.packets.SensorDataPayload;
import org.rdfkad.rules.TemperatureRule;
import org.rdfkad.tables.NodeConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensorMulticastReceiver  implements  Runnable{

    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final int PORT = 4446;
// The unique ID this client responds to
    Pattern pattern = Pattern.compile("\\d+"); // Regex for one or more digits


    public  void run() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            socket.joinGroup(group);
//            Rules rules = new Rules();
//            RulesEngine rulesEngine = new DefaultRulesEngine();
//            TemperatureRule temperatureRule = new TemperatureRule();
//            rules.register(temperatureRule);

//            org.jeasy.rules.api.Facts facts = new org.jeasy.rules.api.Facts();
            while (true) {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                // Deserialize the incoming data
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData());
                try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
                    Object object = objectInputStream.readObject();
                    if (object instanceof SensorDataPayload) {
                        SensorDataPayload payload = (SensorDataPayload) object;
                        int uniqueId = payload.getUniqueId();
                        int multicastId = NodeConfig.getInstance().getMulticastId();
                        // Check if the unique ID matches
                        if (multicastId == uniqueId) {
                            System.out.println("Received relevant payload:");
                            System.out.println("Unique ID: " + payload.getUniqueId());
                            RDFPacket rdfData = payload.getRdfData();

                            int temperature = Integer.parseInt(rdfData.getObject());

//                            facts.put("temperature", temperature);
                            System.out.println("Temperature: " + temperature);

//                            rulesEngine.fire(rules, facts);
                            if (temperature > 30) {
                                System.out.println("Temperature is above 30 degrees");
                            } else {
                                System.out.println("Temperature is below 30 degrees");
                            }
                            System.out.println("RDF Data: Subject - " + rdfData.getSubject() +
                                    ", Predicate - " + rdfData.getPredicate() +
                                    ", Object - " + rdfData.getObject());
                            System.out.println("Sending data for storage");
                            RDFDataHandler rdfDataHandler = new RDFDataHandler();
                            rdfDataHandler.storeRDF(rdfData);
                        }

                        }

                } catch (ClassNotFoundException e) {
                    System.out.println("Error during deserialization: " + e.getMessage());
                }
            }
        } catch (SocketException e) {
            System.out.println("Socket Exception: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Exception: " + e.getMessage());
        }

    }
}
