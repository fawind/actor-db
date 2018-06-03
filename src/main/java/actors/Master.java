package actors;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import messages.query.CreateTableMsg;
import messages.query.DropTableMsg;
import messages.query.InsertMsg;
import messages.query.InsertRowMsg;
import messages.query.QueryErrorMsg;
import messages.query.QuerySuccessMsg;
import messages.query.SelectAllMsg;
import messages.query.SelectWhereMsg;
import messages.query.TransactionMsg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Master extends AbstractDBActor {

    public static final String ACTOR_NAME = "master";
    private final Map<String, ActorRef> tables;

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
                .match(DropTableMsg.class, this::handleDropTable)
                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }

    private void handleInsert(InsertMsg msg) {
        Optional<ActorRef> table = assertTableExists(msg.getTableName(), msg);
        table.ifPresent(t -> t.tell(new InsertRowMsg(msg.getRow(), msg.getTransaction()), getSender()));
    }

    private void handleCreateTable(CreateTableMsg msg) {
        String tableName = msg.getTableName();
        if (tables.containsKey(tableName)) {
            getSender().tell(new QueryErrorMsg("Table '" + tableName + "' already exists.", msg.getTransaction()),
                    getSelf());
            return;
        }

        String actorName = "table-" + tableName + "_" + (int) (Math.random() * 100);
        ActorRef table = getContext().actorOf(Table.props(msg.getLayout()), actorName);

        log.info("Created actor: " + actorName);

        tables.put(msg.getTableName(), table);
        getSender().tell(new QuerySuccessMsg(msg.getTransaction()), getSelf());
    }

    private void handleDropTable(DropTableMsg msg) {
        String tableName = msg.getTableName();

        ActorRef table = tables.remove(tableName);
        if (table == null) {
            getSender().tell(new QueryErrorMsg("Table '" + tableName + "' doesn't exists.", msg.getTransaction()),
                    getSelf());
            return;
        }

        table.tell(PoisonPill.getInstance(), ActorRef.noSender());
    }

    private void handleSelectAll(SelectAllMsg msg) {
        Optional<ActorRef> table = assertTableExists(msg.getTableName(), msg);
        table.ifPresent(t -> t.tell(msg, getSender()));
    }

    private void handleSelectWhere(SelectWhereMsg msg) {
        Optional<ActorRef> table = assertTableExists(msg.getTableName(), msg);
        table.ifPresent(t -> t.tell(msg, getSender()));
    }

    private Optional<ActorRef> assertTableExists(String tableName, TransactionMsg msg) {
        ActorRef table = tables.get(tableName);
        if (table == null) {
            getSender().tell(new QueryErrorMsg("Table '" + tableName + "' does not exist.", msg.getTransaction()),
                    getSelf());
            return Optional.empty();
        } else {
            return Optional.of(table);
        }
    }
}
