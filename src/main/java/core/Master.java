package core;

import akka.actor.AbstractActor;
import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.CreateTableMsg;
import messages.InsertMsg;
import messages.InsertRowMsg;
import messages.QuerySuccessMsg;
import messages.SelectAllMsg;

import java.util.HashMap;
import java.util.Map;

public class Master extends AbstractDBActor {
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

    private void handleInsertMsg(InsertMsg insertMsg) {
        tables.get(insertMsg.getTableName()).tell(new InsertRowMsg(insertMsg.getRow()), getSelf());
    }

    private void handleCreateTableMsg(CreateTableMsg createTableMsg) {
        log.info("createTableMsg = [" + createTableMsg.getName()+ ": " + createTableMsg.getLayout() + "]");

        String actorName = createTableMsg.getName() + "-actor";
        ActorRef table = getContext().actorOf(Table.props(createTableMsg.getName(), createTableMsg.getLayout(), getSelf()), actorName);

        log.info("Created actor: " + actorName);

        tables.put(createTableMsg.getName(), table);
        getSender().tell(new QuerySuccessMsg(), getSelf());
    }

    private void handleSelectAllMsg(SelectAllMsg selectAllMsg) {
        tables.get(selectAllMsg.getTableName()).tell("selectAllMsg", getSender());
    }


    /*
    protected:

    void handleInsert(String tableName, Row row)
    void handleSelectColumn(String tableName, String... columns)
    void handleSelectWhere(String tableName, String column, FilterFn filter)
     */


}
