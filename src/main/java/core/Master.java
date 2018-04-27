package core;

import akka.actor.ActorRef;
import akka.actor.Props;
import messages.CreateTableMsg;
import messages.InsertMsg;
import messages.InsertRowMsg;
import messages.QueryErrorMsg;
import messages.QuerySuccessMsg;
import messages.SelectAllMsg;

import java.util.HashMap;
import java.util.Map;

public class Master extends AbstractDBActor {

    public static final String ACTOR_NAME = "master";

    private Map<String, ActorRef> tables;

    private Master() {
        this.tables = new HashMap<>();
    }

    static Props props() {
        return Props.create(Master.class, Master::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateTableMsg.class, this::handleCreateTableMsg)
                .match(InsertMsg.class, this::handleInsertMsg)
                .match(SelectAllMsg.class, this::handleSelectAllMsg)
                .build();
    }

    private void handleInsertMsg(InsertMsg msg) {
        tables.get(msg.getTableName()).tell(new InsertRowMsg(msg.getRow()), getSelf());
    }

    private void handleCreateTableMsg(CreateTableMsg msg) {
        log.info("createTableMsg = [" + msg.getName()+ ": " + msg.getLayout() + "]");

        String tableName = msg.getName();

        if (tables.containsKey(tableName)) {
            getSender().tell(new QueryErrorMsg("Table '" + tableName + "' already exists."), getSelf());
            return;
        }

        String actorName = "table-actor_" + tableName;
        ActorRef table = getContext().actorOf(Table.props(msg.getName(), msg.getLayout(), getSelf()), actorName);

        log.info("Created actor: " + actorName);

        tables.put(msg.getName(), table);
        getSender().tell(new QuerySuccessMsg(), getSelf());
    }

    private void handleSelectAllMsg(SelectAllMsg msg) {
        tables.get(msg.getTableName()).tell(msg, getSender());
    }


    /*
    protected:

    void handleInsert(String tableName, Row row)
    void handleSelectColumn(String tableName, String... columns)
    void handleSelectWhere(String tableName, String column, FilterFn filter)
     */


}
