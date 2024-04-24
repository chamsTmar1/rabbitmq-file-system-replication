package org.insat.gl3;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelRunner3 implements Runnable {
    private int replicaNumber;

    public ParallelRunner3(int replicaNumber) {
        this.replicaNumber = replicaNumber;
    }

    @Override
    public void run() {
        try {
            // Call Replica's main method with the replicaNumber as argument
            Replica.main(new String[]{String.valueOf(replicaNumber)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Ask the user for the number of instances
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of instances: ");
        int numInstances = scanner.nextInt();
        scanner.close();

        // Create a fixed thread pool with the desired number of threads
        ExecutorService executor = Executors.newFixedThreadPool(numInstances);

        // Submit tasks with different parameters
        for (int i = 1; i <= numInstances; i++) {
            executor.submit(new ParallelRunner3(i));
        }

        // Shutdown the executor when done
        executor.shutdown();
    }
}
