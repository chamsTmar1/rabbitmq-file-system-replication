package org.insat.gl3;


import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SimulationRunner6 {
    private static final int NUM_REPLICAS = 3;
    private static ExecutorService executor = Executors.newFixedThreadPool(NUM_REPLICAS);

    public static void main(String[] args) throws InterruptedException, IOException, TimeoutException {
        // Initialize SimulationRunner instance
        SimulationRunner6 simulationRunner = new SimulationRunner6();
        // Start replicas
        simulationRunner.startReplicas();

        // Write initial data
        simulationRunner.writeData("1 Texte message1");
        simulationRunner.writeData("2 Texte message2");

        // Stop Replica 2
        simulationRunner.stopReplica(2);

        // Write data after failure
        simulationRunner.writeData("3 Texte message3");
        simulationRunner.writeData("4 Texte message4");

        // Restart Replica 2
        simulationRunner.startReplica(2);

        // Wait for replicas to finish
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    private void startReplicas() {
        for (int i = 1; i <= NUM_REPLICAS; i++) {
            startReplica(i);
        }
    }

    private void startReplica(int replicaNumber) {
        executor.submit(() -> {
            try {
                Replica replica = new Replica(replicaNumber);
                replica.start();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        });
    }

    private void stopReplica(int replicaNumber) {
        try {
            for (int i = 1; i <= NUM_REPLICAS; i++) {
                Replica replica = new Replica(i);
                replica.stopInstanceByReplicaNumber(replicaNumber);
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void writeData(String data) {
        try {
            // Pause to simulate time between writes
            Thread.sleep(1000);
            // Write data using the ClientWriter
            ClientWriter.main(new String[]{data});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
