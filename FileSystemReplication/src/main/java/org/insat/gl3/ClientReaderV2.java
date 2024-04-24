package org.insat.gl3;


import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class ClientReaderV2 {
    private final static String READ_ALL_EXCHANGE = "read_all_exchange";
    private final static String READ_ALL_RESPONSE_QUEUE_PREFIX = "read_all_response_queue_";

    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private String readAllRequest = "Read All";

    public ClientReaderV2() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();

        // Declare exchange for "Read All" requests
        channel.exchangeDeclare(READ_ALL_EXCHANGE, BuiltinExchangeType.FANOUT);

        // Declare response queues for "Read All" responses
        for (int i = 1; i <= 3; i++) {
            String queueName = READ_ALL_RESPONSE_QUEUE_PREFIX + i;
            channel.queueDeclare(queueName, false, false, false, null);
            channel.queueBind(queueName, READ_ALL_EXCHANGE, "");
        }
    }

    public void requestReadAll() throws IOException {
        // Send "Read All" request to all replicas
        channel.basicPublish(READ_ALL_EXCHANGE, "", null, readAllRequest.getBytes(StandardCharsets.UTF_8));
        System.out.println(" [x] Requested reading all files from replicas.");
    }

    public Map<Integer, String> readAllLines() throws IOException {
        Map<Integer, String> lines = new HashMap<>();
        // Read responses from all replicas
        for (int i = 1; i <= 3; i++) {
            String queueName = READ_ALL_RESPONSE_QUEUE_PREFIX + i;
            GetResponse response = channel.basicGet(queueName, true);
            if (response != null) {
                String message = new String(response.getBody(), StandardCharsets.UTF_8);
                lines.put(i, message);
            }
        }
        return lines;
    }

    public void close() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }

    public static void main(String[] args) {
        try {
            ClientReaderV2 client = new ClientReaderV2();
            client.requestReadAll();
            Map<Integer, String> lines = client.readAllLines();
            System.out.println("Lines from replicas: " + lines);
            client.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
