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

To run the project:

1. Ensure RabbitMQ is installed and running.
2. Compile and run the Java programs for ClientWriter, ClientReader, and Replica.
3. Follow the prompts or instructions provided by each program to initiate transactions and read/write operations.

## Dependencies

- RabbitMQ Java Client
- Java Development Kit (JDK)
- JTables
