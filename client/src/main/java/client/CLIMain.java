package client;

import api.commands.Command;
import api.configuration.EnvConfig;
import client.commands.CommandParser;
import client.config.DatastoreClientModule;
import api.messages.QueryErrorMsg;
import api.messages.QueryResponseMsg;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.Data;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;

import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;

public class CLIMain {

    @Data
    public static class CLIArgs {
        @Parameter(names = {"--storeHost"}, description = "Host address of the store.")
        private String storeHost = "127.0.0.1";
        @Parameter(names = {"--storePort"}, description = "Port of the store.")
        private int storePort = 2552;
        @Parameter(names = {"--clientPort"}, description = "Port of the client.")
        private int clientPort = 2553;
    }

    private static final String PROMPT = "hakkan-db> ";
    private static final String APP_NAME = "hakkan-db";

    private LineReader lineReader = LineReaderBuilder.builder().appName(APP_NAME).build();

    public static void main(String[] args) {
        CLIArgs arguments = new CLIArgs();
        JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);
        EnvConfig storeEnvConfig = EnvConfig.builder()
                .hostname(arguments.getStoreHost())
                .port(arguments.getStorePort())
                .build();
        EnvConfig clientEnvConfig = EnvConfig.withPort(arguments.getClientPort());
        new CLIMain().run(storeEnvConfig, clientEnvConfig);
    }

    public void run(EnvConfig storeEnvConfig, EnvConfig clientEnvConfig) {
        try (DatastoreClient datastoreClient = DatastoreClientModule.createInstance(clientEnvConfig, storeEnvConfig)) {
            datastoreClient.start();
            readLines(datastoreClient);
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
        response.thenAccept(msg -> {
            if (msg instanceof QueryErrorMsg) {
                System.err.println(">> [ERROR] " + ((QueryErrorMsg) msg).getMsg());
            } else {
                System.out.println(">> " + msg.getQueryMetaInfo());
            }
        });
    }
}
