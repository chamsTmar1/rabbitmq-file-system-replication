package org.insat.gl3;


import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ClientReader {
    private final static String READ_LAST_EXCHANGE = "read_last_exchange";
    private final static String RESPONSE_QUEUE = "response_queue";
    private final static String READ_REQUEST_MESSAGE = "Read Last";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // Déclaration de l'échange et de la queue pour la réponse
            channel.exchangeDeclare(READ_LAST_EXCHANGE, BuiltinExchangeType.FANOUT);
            String queueName = channel.queueDeclare(RESPONSE_QUEUE, false, false, false, null).getQueue();
            channel.queueBind(queueName, READ_LAST_EXCHANGE, "");

            // Envoyer la requête de lecture
            channel.basicPublish(READ_LAST_EXCHANGE, "", null, READ_REQUEST_MESSAGE.getBytes("UTF-8"));
            System.out.println(" [x] Requête de lecture envoyée.");

            // Écouter la réponse du replica
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Réponse du Replica : '" + message + "'");
                // Affichage de la réponse et fermeture du programme
                System.out.println(" [x] Dernière ligne du fichier texte : '" + message + "'");
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        }
    }
}
