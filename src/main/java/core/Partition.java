package core;

import akka.actor.Props;
import messages.InsertRowMsg;
import messages.SuccessMsg;

import java.util.ArrayList;
import java.util.List;

public class Partition extends AbstractDBActor {
    private final List<Row> rows;

    private Partition() {
        this.rows = new ArrayList<>(1000);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InsertRowMsg.class, this::handleInsert)
                .build();
    }

    public static Props props() {
        return Props.create(Partition.class);
    }

    private void handleInsert(InsertRowMsg rowMsg) {
        rows.add(rowMsg.getRow());
        log.info("Added row: " + rowMsg.getRow());
        getSender().tell(new SuccessMsg(), getSelf());
    }


    /*
    protected:

    void handleSelectAll()
    void handleSelectColumn(String... columns)
    void handleSelectWhere(String column, FilterFn filter)

    Array[N] rows
     */
}
