package core;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import messages.InsertRowMsg;
import messages.PartitionBlockedMsg;
import messages.PartitionFullMsg;
import messages.SplitPartitionMsg;
import messages.SplitSuccessMsg;
import messages.SuccessMsg;

import java.util.Collection;

public class Table extends AbstractDBActor {
    private final String name;
    private final String layout;
    private final ActorRef master;
    private int highestPartitionId = 1;

    private final Multimap<ActorRef, Row> blockedRows;

    private RangeMap<Long, ActorRef> partitions = TreeRangeMap.create();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InsertRowMsg.class, this::handleInsert)
                .match(PartitionFullMsg.class, this::handlePartitionFull)
                .match(PartitionBlockedMsg.class, this::handlePartitionBlocked)
                .match(SplitSuccessMsg.class, this::handleSplitSuccess)
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

        this.blockedRows = MultimapBuilder.hashKeys().arrayListValues().build();

        Range<Long> startRange = Range.closed(Long.MIN_VALUE, Long.MAX_VALUE);
        ActorRef partition = createPartition(startRange);
        partitions.put(startRange, partition);
    }

    private void handleInsert(InsertRowMsg msg) {
        ActorRef partition = partitions.get(msg.getRow().getHashKey());
        partition.tell(msg, getSelf());
    }

    private void handlePartitionFull(PartitionFullMsg msg) {
        ActorRef newPartition = createPartition(msg.getNewRange());

        msg.getPartition().tell(new SplitPartitionMsg(newPartition), getSelf());
    }

    private void handlePartitionBlocked(PartitionBlockedMsg msg) {
        blockedRows.put(getSender(), msg.getRow());
    }

    private void handleSplitSuccess(SplitSuccessMsg msg) {
        Range<Long> oldRange = partitions.getEntry(msg.getOldRange().lowerEndpoint()).getKey();
        partitions.remove(oldRange);

        partitions.put(msg.getOldRange(), msg.getOldPartition());
        partitions.put(msg.getNewRange(), msg.getNewPartition());

        Collection<Row> rows = blockedRows.get(msg.getOldPartition());
        for (Row row : rows) {
            ActorRef partition = partitions.get(row.getHashKey());
            partition.tell(new InsertRowMsg(row), getSelf());
        }

        rows.clear();
    }

    private ActorRef createPartition(Range<Long> range) {
        int partitionId = highestPartitionId++;
        log.info("Created new partition (" + range + ")");
        ActorRef partition = getContext().actorOf(Partition.props(partitionId, range, getSelf()), String.valueOf(partitionId));
        partitions.put(range, partition);
        return partition;
    }


    /*
    protected:

    void handleSelectAll()
    void handleSelectColumn(String... columns)
    void handleSelectWhere(String column, FilterFn filter)


     */

}
