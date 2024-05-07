package org.insat.gl3;


import com.rabbitmq.client.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeoutException;


public class ClientReaderV2 {
    private static final String READ_ALL_EXCHANGE_NAME = "read_all_exchange";
    private static final String READ_ALL_QUEUE = "read_all_queue";
    private static final int NBR_REPLICAS = 3;
    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(READ_ALL_EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            channel.queueDeclare(READ_ALL_QUEUE, false, false, false, null);

            String message = "Read All";
            channel.basicPublish(READ_ALL_EXCHANGE_NAME, "", null, message.getBytes());
            System.out.println(" [x] a envoyé la requête 'Read All' aux réplicats");

            // Ecouter les réponses de tous les réplicats
            Map<String, Integer> lineCount = new HashMap<>();

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String response = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] a reçu des réplicats: " + response);
                // Mettre à jour lineCount
                lineCount.put(response, lineCount.getOrDefault(response, 0) + 1);
            };

            channel.basicConsume(READ_ALL_QUEUE, true, deliverCallback, consumerTag -> {});

            // On attend que tts les réplicats ont répondu par leur lineCount
            // avant de chercher la majorité des lignes
            Thread.sleep(3000);

            // Trouver les lignes qui apparaissent dans la majorité des réplicats
            List<String> majLines = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : lineCount.entrySet()) {
                if (entry.getValue() > NBR_REPLICAS / 2) {
                    majLines.add(entry.getKey());
                }
            }

            // Afficher les lignes qui apparaissent dans la majorité des réplicats
            System.out.println("Lignes qui apparaissent dans la majorité des réplicats:");
            for (String line : majLines) {
                System.out.println(line);
            }

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
