package org.insat.gl3;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class ClientReader {
    private static final String READ_LAST_EXCHANGE_NAME = "read_last_exchange";
    private static final String READ_LAST_QUEUE = "read_last_queue";

    public static void main(String[] args) {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(READ_LAST_EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            channel.queueDeclare(READ_LAST_QUEUE, false, false, false, null);

            String message = "Read Last";
            channel.basicPublish(READ_LAST_EXCHANGE_NAME, "", null, message.getBytes());
            System.out.println(" [x] a envoyé la requête 'Read Last' aux réplicats");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String response = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] a reçu des réplicats: " + response);
            };

            channel.basicConsume(READ_LAST_QUEUE, true, deliverCallback, consumerTag -> {});

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
