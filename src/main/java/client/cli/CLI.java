package client.cli;

import api.commands.Command;
import client.DatastoreClient;
import client.config.DatastoreClientModule;
import configuration.DatastoreModule;
import configuration.EnvConfig;
import core.Datastore;
import messages.query.QueryResponseMsg;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;

import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;

public class CLI {

    private static final String PROMPT = "hakkan-db> ";
    private static final String APP_NAME = "hakkan-db";

    private LineReader lineReader = LineReaderBuilder.builder().appName(APP_NAME).build();

    public static void main(String[] args) {
        new CLI().run();
    }

    public void run() {
        // TODO: Add configurable run configs for cli-only and local nodes
        try (Datastore datastore = DatastoreModule.createInstance(EnvConfig.withPort(2552))) {
            datastore.start();
            try (DatastoreClient datastoreClient = DatastoreClientModule.createInstance("akka.tcp://actors-db@127.0.0.1:2552/system/receptionist")) {
                datastoreClient.start();
                readLines(datastoreClient);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readLines(DatastoreClient datastoreClient) {
        while (true) {
            try {
                String line = lineReader.readLine(PROMPT);
                try {
                    executeCommand(datastoreClient, line);
                } catch (IllegalArgumentException e) {
                    System.out.println(format("Error: %s", e.getMessage()));
                }
            } catch (UserInterruptException | EndOfFileException e) {
                return;
            }
        }
    }

    private void executeCommand(DatastoreClient datastoreClient, String message) {
        if (message == null || message.length() == 0) {
            return;
        }
        Command command = CommandParser.fromLine(message);
        CompletableFuture<QueryResponseMsg> response = datastoreClient.sendRequest(command);
        response.thenAccept(msg -> System.out.println(">> " + msg));
    }
}
