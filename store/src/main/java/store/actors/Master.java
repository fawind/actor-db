package store.actors;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import api.messages.LamportId;
import api.messages.QueryErrorMsg;
import api.messages.QueryMetaInfo;
import api.messages.QueryMsg;
import api.messages.QuerySuccessMsg;
import store.messages.query.CreateTableMsg;
import store.messages.query.DeleteKeyMsg;
import store.messages.query.DropTableMsg;
import store.messages.query.InsertMsg;
import store.messages.query.InsertRowMsg;
import store.messages.query.SelectAllMsg;
import store.messages.query.SelectKeyMsg;
import store.messages.query.SelectWhereMsg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Master extends AbstractDBActor {

    public static final String ACTOR_NAME = "master";
    private final Map<String, ActorRef> tables;

    private final String id = UUID.randomUUID().toString();
    private LamportId lamportId = new LamportId(id);

    private Master() {
        tables = new HashMap<>();
    }

    public static Props props() {
        return Props.create(Master.class, Master::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateTableMsg.class, this::handleCreateTable)
                .match(InsertMsg.class, this::handleInsert)
                .match(SelectAllMsg.class, this::handleSelectAll)
                .match(SelectWhereMsg.class, this::handleSelectWhere)
                .match(SelectKeyMsg.class, this::handleSelectKey)
                .match(DeleteKeyMsg.class, this::handleDeleteKey)
                .match(DropTableMsg.class, this::handleDropTable)
                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }

    private void handleInsert(InsertMsg msg) {
        Optional<ActorRef> table = assertTableExists(msg.getTableName(), msg);
        table.ifPresent(t -> t.tell(new InsertRowMsg(msg.getRow(), addResponseLamportIdToMeta(msg.getQueryMetaInfo())
        ), getSender()));
    }

    private void handleCreateTable(CreateTableMsg msg) {
        String tableName = msg.getTableName();
        if (tables.containsKey(tableName)) {
            getSender().tell(new QueryErrorMsg("Table '" + tableName + "' already exists.",
                    addResponseLamportIdToMeta(msg.getQueryMetaInfo())), getSelf());
            return;
        }

        String actorName = Table.ACTOR_NAME + "-" + tableName;
        ActorRef table = getContext().actorOf(Table.props(msg.getLayout()), actorName);

        log.info("Created actor: " + actorName);

        tables.put(msg.getTableName(), table);
        getSender().tell(new QuerySuccessMsg(addResponseLamportIdToMeta(msg.getQueryMetaInfo())), getSelf());
    }

    private void handleDropTable(DropTableMsg msg) {
        String tableName = msg.getTableName();

        ActorRef table = tables.remove(tableName);
        if (table == null) {
            getSender().tell(new QueryErrorMsg("Table '" + tableName + "' doesn't exists.",
                    addResponseLamportIdToMeta(msg.getQueryMetaInfo())), getSelf());
            return;
        }

        table.tell(PoisonPill.getInstance(), ActorRef.noSender());
    }

    private void handleSelectAll(SelectAllMsg msg) {
        Optional<ActorRef> table = assertTableExists(msg.getTableName(), msg);
        table.ifPresent(t -> tellWithResponseLamportId(t, msg));
    }

    private void handleSelectWhere(SelectWhereMsg msg) {
        Optional<ActorRef> table = assertTableExists(msg.getTableName(), msg);
        table.ifPresent(t -> tellWithResponseLamportId(t, msg));
    }

    private void handleSelectKey(SelectKeyMsg msg) {
        Optional<ActorRef> table = assertTableExists(msg.getTableName(), msg);
        table.ifPresent(t -> tellWithResponseLamportId(t, msg));
    }

    private void handleDeleteKey(DeleteKeyMsg msg) {
        Optional<ActorRef> table = assertTableExists(msg.getTableName(), msg);
        table.ifPresent(t -> tellWithResponseLamportId(t, msg));
    }

    private Optional<ActorRef> assertTableExists(String tableName, QueryMsg msg) {
        ActorRef table = tables.get(tableName);
        if (table == null) {
            getSender().tell(new QueryErrorMsg("Table '" + tableName + "' does not exist.",
                    addResponseLamportIdToMeta(msg.getQueryMetaInfo())), getSelf());
            return Optional.empty();
        } else {
            return Optional.of(table);
        }
    }

    private void tellWithResponseLamportId(ActorRef to, QueryMsg msg) {
        msg.updateMetaInfo(addResponseLamportIdToMeta(msg.getQueryMetaInfo()));
        to.tell(msg, getSender());
    }

    private QueryMetaInfo addResponseLamportIdToMeta(QueryMetaInfo metaInfo) {
        return metaInfo.copyWithResponseLamportId(updatedLamportId(metaInfo.getLamportId()));
    }

    private LamportId updatedLamportId(LamportId other) {
        lamportId = lamportId.maxIdCopy(other);
        return lamportId;
    }
}
