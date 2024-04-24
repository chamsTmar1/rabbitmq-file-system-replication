package org.insat.gl3;

import com.rabbitmq.client.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

public class Replica {
    private final static String EXCHANGE_NAME = "echange_fichiers";
    private final static String QUEUE_NAME_PREFIX = "replica_queue_";
    private final static String READ_LAST_EXCHANGE = "read_last_exchange";
    private final static String RESPONSE_QUEUE = "response_queue";
    private final static String READ_ALL_EXCHANGE = "read_all_exchange";
    private final static String READ_ALL_RESPONSE_QUEUE = "read_all_response_queue";

    private int replicaNumber;
    private ConnectionFactory factory;
    private static Connection connection;
    private static Channel channel;

    public Replica(int replicaNumber) {
        this.replicaNumber = replicaNumber;
    }

    public void start() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        channel.exchangeDeclare(READ_LAST_EXCHANGE, BuiltinExchangeType.FANOUT);
        channel.exchangeDeclare(READ_ALL_EXCHANGE, BuiltinExchangeType.FANOUT);

        String queueName = QUEUE_NAME_PREFIX + replicaNumber;
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        channel.queueDeclare(RESPONSE_QUEUE, false, false, false, null);
        channel.queueBind(RESPONSE_QUEUE, EXCHANGE_NAME, "");

        channel.queueDeclare(READ_ALL_RESPONSE_QUEUE + "_" + replicaNumber, false, false, false, null);
        channel.queueBind(READ_ALL_RESPONSE_QUEUE + "_" + replicaNumber, READ_ALL_EXCHANGE, "");


        System.out.println(" [*] Attente des messages. Pour quitter, appuyez sur CTRL+C");


        // Callback for reading last line or all lines
        DeliverCallback readCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            String responseQueueName = delivery.getProperties().getReplyTo();
            if (message.equals("Read Last")) {
                String lastLine = readLastLine(replicaNumber);
                channel.basicPublish("", responseQueueName, null, lastLine.getBytes(StandardCharsets.UTF_8));
            } else if (message.equals("Read All")) {
                readAllLines(responseQueueName);
            }
        };

        // Ecoute de la file de réponse pour la tâche de lecture (all/last)
        channel.basicConsume(queueName, true, readCallback, consumerTag -> {});

        // Callback pour les tâches d'écriture
        DeliverCallback writeCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Reçu '" + message + "'");
            writeToTextFile(message, replicaNumber);
        };

        // Ecoute de la file pour les tâches d'écriture
        channel.basicConsume(queueName, true, writeCallback, consumerTag -> {});
    }

    public void stopInstanceByReplicaNumber(int replicaNumberToStop) throws IOException, TimeoutException {
        if (this.replicaNumber == replicaNumberToStop) {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
            System.out.println("Instance stopped for replica number: " + replicaNumberToStop);
        } else {
            System.out.println("This instance is not responsible for replica number: " + replicaNumberToStop);
        }
    }

    private void writeToTextFile(String message, int replicaNumber) {
        String fileName = "replica_" + replicaNumber + ".txt";
        try (FileWriter writer = new FileWriter(fileName, true)) {
            // Ecriture du message dans le fichier texte
            writer.write(message + "\n");
            System.out.println(" [x] Message écrit dans le fichier '" + fileName + "'");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readAllLines(String responseQueueName) {
        String fileName = "replica_" + replicaNumber + ".txt";
        try {
            Files.lines(Paths.get(fileName)).forEach(line -> {
                try {
                    channel.basicPublish("", responseQueueName, null, line.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readLastLine(int replicaNumber) {
        String fileName = "replica_" + replicaNumber + ".txt";
        try {
            // Lecture de la dernière ligne du fichier texte
            String lastLine = Files.lines(Paths.get(fileName)).reduce((a, b) -> b).orElse("");
            System.out.println(" [x] Dernière ligne du fichier '" + fileName + "' : '" + lastLine + "'");
            return lastLine;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


    public static void main(String[] args) throws IOException, TimeoutException {
        if (args.length != 1) {
            System.err.println("Usage: Replica <numéro>");
            System.exit(1);
        }

        int replicaNumber = Integer.parseInt(args[0]);
        Replica replica = new Replica(replicaNumber);
        replica.start();
    }
}
