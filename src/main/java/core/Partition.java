package core;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.google.common.collect.Range;
import messages.InsertRowMsg;
import messages.PartialSplitSuccessMsg;
import messages.PartitionBlockedMsg;
import messages.PartitionFullMsg;
import messages.SplitInsertMsg;
import messages.SplitPartitionMsg;
import messages.SplitSuccessMsg;
import messages.SuccessMsg;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Partition extends AbstractDBActor {
    private final int partitionId;
    private List<Row> rows;
    private Range<Long> range;
    private final ActorRef table;

    private final int capacity = 10;
    private boolean isFull = false;

    private Partition(int partitionId, Range<Long> startRange, ActorRef table) {
        this.partitionId = partitionId;
        this.range = startRange;
        this.table = table;
        this.rows = new ArrayList<>(capacity);

        log.info("Range: " + range);
    }

    public static Props props(int partitionId, Range<Long> startRange, ActorRef table) {
        return Props.create(Partition.class, () -> new Partition(partitionId, startRange, table));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InsertRowMsg.class, this::handleInsert)
                .match(SplitPartitionMsg.class, this::handleSplitPartition)
                .match(SplitInsertMsg.class, this::handleSplitInsert)
                .match(PartialSplitSuccessMsg.class, this::handlePartialSplitSuccess)
                .build();
    }

    private void handleInsert(InsertRowMsg msg) {
        if (isFull) {
            getSender().tell(new PartitionBlockedMsg(msg.getRow()), getSelf());
            return;
        }

        rows.add(msg.getRow());
        log.info("Added row: " + msg.getRow());


        if (rows.size() == capacity) {
            isFull = true;
            rows.sort(Comparator.naturalOrder());

            long lowestInNewPartition = rows.get(capacity / 2).getHashKey();

            Range<Long> newRange = Range.closed(lowestInNewPartition, range.upperEndpoint());

            // We want to cover all values up to the lowest in the new partition
            range = Range.closedOpen(range.lowerEndpoint(), lowestInNewPartition);

            getSender().tell(new PartitionFullMsg(getSelf(), range, newRange), getSelf());
        }

        getSender().tell(new SuccessMsg(), getSelf());
    }

    private void handleSplitPartition(SplitPartitionMsg msg) {
        ActorRef other = msg.getNewPartition();

        List<Row> rowsToCopy = new ArrayList<>(rows.subList(capacity / 2, capacity));
        other.tell(new SplitInsertMsg(rowsToCopy), getSelf());

    }

    private void handleSplitInsert(SplitInsertMsg msg) {
        this.rows = msg.getRows();
        log.info("Inserted new rows");
        getSender().tell(new PartialSplitSuccessMsg(getSelf(), range), getSelf());
    }

    private void handlePartialSplitSuccess(PartialSplitSuccessMsg msg) {
        rows = new ArrayList<>(rows.subList(0, capacity / 2));
        isFull = false;

        table.tell(new SplitSuccessMsg(msg.getNewPartition(), msg.getNewRange(), getSelf(), range), getSelf());
    }


    /*
    protected:

    void handleSelectAll()
    void handleSelectColumn(String... columns)
    void handleSelectWhere(String column, FilterFn filter)

    Array[N] rows
     */
}
