# rabbitmq-file-system-replication

This project is a Java-based data replication system utilizing RabbitMQ messaging middleware to ensure data consistency and availability. It provides a framework for replicating text files across multiple replicas on a single machine.

## Description

The project implements two strategies for data replication:

1. **Consistency Strategy:** Prioritizes data consistency across replicas.
2. **Availability-Consistency Tradeoff Strategy:** Balances availability and consistency.

The project consists of three main processes:

1. **ClientWriter:** A Java program responsible for initiating transactions to add lines of text to files.
2. **ClientReader:** A Java program for reading lines of text from files.
3. **Replica:** A Java program that handles client requests for both read and write operations.

Please note that comments and documentation within the project are in French.

## Usage

To test the project:

1. Ensure RabbitMQ Server is installed and running (localhost:15672 most of the time).
2. Run the `ClientWriter` process to emit a message adding a line of text to the RabbitMQ broker.
3. Launch the `Replica` processes to read the corresponding messages from their respective RabbitMQ queues and add them to their local file.
--> Each `Replica` process should be launched with an argument indicating its number, for example: Java Replica 1 
4. Open the files inside Rep1,2 and 3 to ensure proper replication of data across the different replicas.
5. Keep the replicas processes running and run ClientReader to get the last line of each file
6. Now, Simulate a failure by stopping the execution of one of the replicas, then perform a read with the client to observe how replication ensures data availability.
7. Conduct an execution simulation where the `ClientWriter` writes two lines of data, then stops Replica 2, writes two more lines, and then restarts Replica 2. Verify that the files in all three replicas do not contain the same data.
8. Keep the replicas processes running and run ClientReaderV2 to get the lines that appear in the majority of the files
**N.B :** If you're using Intellij idea IDE for Java (as this is a maven project -> IDE helps a lot) , to run Replica processes in parallel with different argument each time, execute it one time on the IDE then copy the command from console and paste it in three instances of command prompts, each time changing the argument. 
## Dependencies

- RabbitMQ Java Client
- Java Development Kit (JDK)
