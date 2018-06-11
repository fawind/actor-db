package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import api.commands.ClientRequest;
import api.commands.CreateTableCommand;
import api.commands.InsertIntoCommand;
import api.commands.SelectAllCommand;
import api.messages.LamportId;

// TODO: Add mapping from request to result
public class ClientEndpoint extends AbstractDBActor {

    public static final String ACTOR_NAME = "client-endpoint";

    public static Props props(ActorRef quorumManager) {
        return Props.create(ClientEndpoint.class, () -> new ClientEndpoint(quorumManager));
    }

    private final ActorRef quorumManager;

    public ClientEndpoint(ActorRef quorumManager) {
        this.quorumManager = quorumManager;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ClientRequest.class, this::handleRequest)
                .build();
    }

    private void handleRequest(ClientRequest request) {
        switch (request.getCommand().getCommandType()) {
            case CREATE_TABLE:
                CreateTableCommand createCommand = (CreateTableCommand) request.getCommand();
                createTable(createCommand, request.getLamportId());
                break;
            case INSERT_INTO:
                InsertIntoCommand insertCommand = (InsertIntoCommand) request.getCommand();
                insertInto(insertCommand, request.getLamportId());
                break;
            case SELECT_ALL:
                SelectAllCommand selectAllCommand = (SelectAllCommand) request.getCommand();
                selectAllFrom(selectAllCommand, request.getLamportId());
                break;
            default:
                log.error("Invalid command: {}", request.getCommand().getCommandType());
                break;
        }
    }

    private void createTable(CreateTableCommand command, LamportId lamportId) {
        log.info("Create Table");
        // TODO: QuorumMananger.tell()
    }

    private void insertInto(InsertIntoCommand command, LamportId lamportId) {
        log.info("Insert Into");
    }

    private void selectAllFrom(SelectAllCommand command, LamportId lamportId) {
        log.info("Select All From");
    }
}
