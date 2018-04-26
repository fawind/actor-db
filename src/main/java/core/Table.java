package core;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import messages.InsertRowMsg;
import messages.SuccessMsg;

public class Table extends AbstractDBActor {
    private final String name;
    private final String layout;
    private final ActorRef master;

    private RangeMap<String, ActorRef> partitions = TreeRangeMap.create();


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InsertRowMsg.class, this::handleInsert)
                .match(SuccessMsg.class, msg -> master.tell(msg, getSelf()))
                .build();
    }

    static Props props(String tableName, String layout, ActorRef master) {
        return Props.create(Table.class, () -> new Table(tableName, layout, master));
    }

    private Table(String name, String layout, ActorRef master) {
        this.name = name;
        this.layout = layout;
        this.master = master;

        log.info("Created first partition (range all)");
        ActorRef partition = getContext().actorOf(Partition.props(), "1");
        partitions.put(Range.all(), partition);
    }

    private void handleInsert(InsertRowMsg rowMsg) {
        ActorRef partition = partitions.get(rowMsg.getRow().getAt(0));
        partition.tell(rowMsg, getSelf());
    }


    /*
    protected:

    void handleSelectAll()
    void handleSelectColumn(String... columns)
    void handleSelectWhere(String column, FilterFn filter)


     */

}
