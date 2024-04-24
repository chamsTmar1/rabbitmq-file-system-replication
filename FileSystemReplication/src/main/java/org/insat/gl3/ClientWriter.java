package org.insat.gl3;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class ClientWriter {
    private final static String EXCHANGE_NAME = "echange_fichiers";

    public static void main(String[] argv) throws Exception {
        // Création d'une connexion à RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // Spécifier l'hôte RabbitMQ
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // Déclaration de la file d'attente à utiliser
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

            String message = getMessage(argv);
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
            System.out.println(" [x] Envoyé '" + message + "'");

        }
    }

    private static String getMessage(String[] strings) {
        if (strings.length < 1) {
            return "Pas de Contenu !!";
        }
        return String.join(" ", strings);
    }
}

