package client.model;

import api.commands.Command;
import lombok.Data;
import messages.query.QueryResponseMsg;

import java.util.concurrent.CompletableFuture;

@Data
public class CompletableCommand {

    private final Command command;
    private final CompletableFuture<QueryResponseMsg> response;

    public static CompletableCommand fromCommand(Command command) {
        return new CompletableCommand(command, new CompletableFuture<>());
    }
}
