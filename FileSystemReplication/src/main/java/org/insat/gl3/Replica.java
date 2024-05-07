package org.insat.gl3;

import com.rabbitmq.client.*;

import java.io.*;
import java.util.concurrent.TimeoutException;

public class Replica {

    private static final String EXCHANGE_NAME = "write_exchange";
    private static final int NBR_REPLICAS = 3;

    private static final String PATH = "C:\\Users\\chams\\Documents\\rabbitmq-file-system-replication\\FileSystemReplication";

    private static final String READ_LAST_EXCHANGE_NAME = "read_last_exchange";
    private static final String READ_LAST_QUEUE = "read_last_queue";

    private static final String READ_ALL_EXCHANGE_NAME = "read_all_exchange";
    private static final String READ_ALL_QUEUE = "read_all_queue";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Essayez : java Replica <replica_number>");
            System.exit(1);
        }

        int replicaId = Integer.parseInt(args[0]);

        if (replicaId < 1 || replicaId > NBR_REPLICAS) {
            System.out.println("Le nombre des réplicats doit être entre 1 et " + NBR_REPLICAS);
            System.exit(1);
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try {

             Connection connection = factory.newConnection();
             Channel channel = connection.createChannel();

             // 1 - Consommer et Écrire les messages de ClientWriter
            String queueName =  channel.queueDeclare().getQueue();
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            channel.queueBind(queueName, EXCHANGE_NAME, "");

            System.out.println(" [*] Réplicat " + replicaId + " en attente des messages.");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] a Reçu '" + message + "'");
                writeToTextFile(message, replicaId);
            };

            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });

            // 2 - Recevoir les requêtes de ClientReader et renvoyer le résultat
            channel.exchangeDeclare(READ_LAST_EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            String readLastQueue = channel.queueDeclare().getQueue();
            channel.queueBind(readLastQueue, READ_LAST_EXCHANGE_NAME, "");

            channel.queueDeclare(READ_LAST_QUEUE, false, false, false, null);

            DeliverCallback readLastCallback = (consumerTag, delivery) -> {

                System.out.println(" [x] a reçu la requête 'Read Last' de ClientReader");

                String lastLine = readLastLine(PATH + "\\Rep" + replicaId + "\\fichier.txt");

                channel.basicPublish("", READ_LAST_QUEUE, null, lastLine.getBytes("UTF-8"));
            };

            channel.basicConsume(readLastQueue, true, readLastCallback, consumerTag -> {
            });

            // 3 - Recevoir les requêtes de ClientReaderV2 et renvoyer le résultat
            channel.exchangeDeclare(READ_ALL_EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            String readAllQueue = channel.queueDeclare().getQueue();
            channel.queueBind(readAllQueue, READ_ALL_EXCHANGE_NAME, "");

            DeliverCallback readAllCallback = (consumerTag, delivery) -> {

                System.out.println(" [x] a reçu la requête 'Read All' de ClientReaderV2");

                // Lire tout le fichier
                // Envoyer chaque ligne dans un message
                try (BufferedReader reader = new BufferedReader(new FileReader(PATH + "\\Rep" + replicaId + "\\fichier.txt"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        channel.basicPublish("", READ_ALL_QUEUE, null, line.getBytes("UTF-8"));
                        System.out.println(" [x] a envoyé à read_all_queue la ligne: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Erreur de lecture du fichier: " + e.getMessage());
                }
            };

            channel.basicConsume(readAllQueue, true, readAllCallback, consumerTag -> {});

        } catch (IOException | TimeoutException e) {
            System.err.println(" [!] Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void writeToTextFile(String message, int replicaId) {
        String fileName = PATH + "\\Rep" + replicaId + "\\fichier.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(message + "\n");
            System.out.println(" [x] Ligne ajoutée à : " + fileName);
        } catch (IOException e) {
            System.err.println(" [!] Erreur lors de l'ajout d'une ligne : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String readLastLine(String fileName) {

        String lastLine = null;

        try (RandomAccessFile file = new RandomAccessFile(new File(fileName), "r")) {

            long fileLength = file.length();

            // Si le fichier est vide
            if (fileLength == 0) {
                return null;
            }

            // Commencer à la fin du fichier
            long pos = fileLength - 2;

            // string_buffer construit la dernière ligne du fichier
            // en lisant les caractères à partir de la fin.
            String string_buffer = "";
            boolean foundNewLine = false;

            // Lire les caractères à partie de la fin jusqu'à trouver \n
            // ou atteindre le début du fichier (pos >= 0)
            while (pos >= 0) {
                file.seek(pos);
                char c = (char) file.read();
                if (c == '\n') {
                    // Si on trouve '\n' (retour à la ligne)
                    foundNewLine = true;
                    break;
                }
                string_buffer = c + string_buffer;
                pos--;
            }

            lastLine = string_buffer;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur de lecture du fichier: " + e.getMessage());
        }
        return lastLine;
    }

}

/*
* To run on terminal :
* "C:\Program Files\Zulu\zulu-11\bin\java.exe" "-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA 2023.3.4\lib\idea_rt.jar=53988:C:\Program Files\JetBrains\IntelliJ IDEA 2023.3.4\bin" -Dfile.encoding=UTF-8 -classpath C:\Users\chams\Documents\rabbitmq-file-system-replication\FileSystemReplication\target\classes;C:\Users\chams\.m2\repository\org\slf4j\slf4j-api\2.1.0-alpha1\slf4j-api-2.1.0-alpha1.jar;C:\Users\chams\.m2\repository\com\rabbitmq\amqp-client\5.20.0\amqp-client-5.20.0.jar org.insat.gl3.Replica 1
 * */