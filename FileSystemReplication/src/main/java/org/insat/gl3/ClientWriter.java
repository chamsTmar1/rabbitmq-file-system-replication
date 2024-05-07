package org.insat.gl3;


import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class ClientWriter {

    private static final String EXCHANGE_NAME = "write_exchange";

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try(Connection connection = factory.newConnection();
        Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            String message = getMessage(argv);

            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
            System.out.println("[x] a envoyé '" + message + "'");
        }

    }

    private static String getMessage(String[] strings) {
        if (strings.length < 1) {
            return "Message par Défaut";
        }
        return String.join(" ", strings);
    }
}