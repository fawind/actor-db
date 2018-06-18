package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import api.commands.ClientRequest;
import api.commands.CreateTableCommand;
import api.commands.InsertIntoCommand;
import api.commands.SelectAllCommand;
import api.commands.SelectWhereCommand;
import api.messages.LamportId;
import api.messages.QueryMetaInfo;
import messages.query.CreateTableMsg;
import messages.query.InsertMsg;
import messages.query.SelectAllMsg;
import messages.query.SelectWhereMsg;
import model.Row;

public class ClientEndpoint extends AbstractDBActor {

    public static final String ACTOR_NAME = "client-endpoint";
    private final ActorRef quorumManager;

    public ClientEndpoint(ActorRef quorumManager) {
        this.quorumManager = quorumManager;
    }

    public static Props props(ActorRef quorumManager) {
        return Props.create(ClientEndpoint.class, () -> new ClientEndpoint(quorumManager));
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
                createTable(createCommand, request.getClientRequestId(), request.getLamportId());
                break;
            case INSERT_INTO:
                InsertIntoCommand insertCommand = (InsertIntoCommand) request.getCommand();
                insertInto(insertCommand, request.getClientRequestId(), request.getLamportId());
                break;
            case SELECT_ALL:
                SelectAllCommand selectAllCommand = (SelectAllCommand) request.getCommand();
                selectAllFrom(selectAllCommand, request.getClientRequestId(), request.getLamportId());
                break;
            case SELECT_WHERE:
                SelectWhereCommand selectWhereCommand = (SelectWhereCommand) request.getCommand();
                selectWhereFrom(selectWhereCommand, request.getClientRequestId(), request.getLamportId());
            default:
                log.error("Invalid command: {}", request.getCommand().getCommandType());
                break;
        }
    }

    private void createTable(CreateTableCommand command, String clientRequestId, LamportId lamportId) {
        QueryMetaInfo queryMetaInfo = QueryMetaInfo.newWriteMeta(getSender(), clientRequestId, lamportId);
        CreateTableMsg msg = new CreateTableMsg(command.getTableName(), command.getSchema(), queryMetaInfo);
        quorumManager.tell(msg, getSelf());
    }

    private void insertInto(InsertIntoCommand command, String clientRequestId, LamportId lamportId) {
        Row row = new Row(command.getValues().toArray(new String[0]));
        QueryMetaInfo queryMetaInfo = QueryMetaInfo.newWriteMeta(getSender(), clientRequestId, lamportId);
        InsertMsg msg = new InsertMsg(command.getTableName(), row, queryMetaInfo);
        quorumManager.tell(msg, getSelf());
    }

    private void selectAllFrom(SelectAllCommand command, String clientRequestId, LamportId lamportId) {
        QueryMetaInfo queryMetaInfo = QueryMetaInfo.newReadMeta(getSender(), clientRequestId, lamportId);
        SelectAllMsg msg = new SelectAllMsg(command.getTableName(), queryMetaInfo);
        quorumManager.tell(msg, getSelf());
    }

    private void selectWhereFrom(SelectWhereCommand command, String clientRequestId, LamportId lamportId) {
        QueryMetaInfo queryMetaInfo = QueryMetaInfo.newReadMeta(getSender(), clientRequestId, lamportId);
        SelectWhereMsg msg = new SelectWhereMsg(command.getTableName(), command.getWhereFn(), queryMetaInfo);
        quorumManager.tell(msg, getSelf());
    }
}
