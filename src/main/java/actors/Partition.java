package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import api.messages.LamportId;
import com.google.common.collect.Range;
import messages.partition.PartialSplitSuccessMsg;
import messages.partition.PartitionBlockedMsg;
import messages.partition.PartitionFullMsg;
import messages.partition.SplitInsertMsg;
import messages.partition.SplitPartitionMsg;
import messages.partition.SplitSuccessMsg;
import messages.query.InsertRowMsg;
import messages.query.PartialQueryResultMsg;
import messages.query.QueryErrorMsg;
import messages.query.QueryMsg;
import messages.query.QuerySuccessMsg;
import messages.query.SelectAllMsg;
import messages.query.SelectWhereMsg;
import model.BlockedRow;
import model.Row;
import model.StoredRow;
import utils.FIFOCache;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Partition extends AbstractDBActor {

    private final int CAPACITY = 100;

    private final int partitionId;
    private final ActorRef table;

    private final Set<LamportId> seenTransactions = FIFOCache.newSet(1024);

    private boolean isFull = false;

    private Range<Long> range;
    private Map<Long, StoredRow> rows;


    private Partition(int partitionId, Range<Long> startRange, ActorRef table) {
        this.partitionId = partitionId;
        this.range = startRange;
        this.table = table;
        this.rows = new HashMap<>(CAPACITY);
    }

    public static Props props(int partitionId, Range<Long> startRange, ActorRef table) {
        return Props.create(Partition.class, () -> new Partition(partitionId, startRange, table));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // Querying
                .match(SelectAllMsg.class, this::handleSelectAll)
                .match(SelectWhereMsg.class, this::handleSelectWhere)
                .match(InsertRowMsg.class, this::handleInsert)

                // Partitioning
                .match(SplitPartitionMsg.class, this::handleSplitPartition)
                .match(SplitInsertMsg.class, this::handleSplitInsert)
                .match(PartialSplitSuccessMsg.class, this::handlePartialSplitSuccess)

                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }

    private void handleSelectAll(SelectAllMsg msg) {
        if (seenTransaction(msg)) return;

        List<StoredRow> resultRows = new ArrayList<>(rows.values());
        getSender().tell(new PartialQueryResultMsg(resultRows, msg.getQueryMetaInfo()), getSelf());
    }

    private void handleSelectWhere(SelectWhereMsg msg) {
        if (seenTransaction(msg)) return;

        Predicate<Row> whereFn = msg.getWhereFn();
        List<StoredRow> resultRows = rows.values().stream()
                .filter(storedRow -> whereFn.test(storedRow.getRow()))
                .collect(Collectors.toList());
        getSender().tell(new PartialQueryResultMsg(resultRows, msg.getQueryMetaInfo()), getSelf());
    }

    private void handleInsert(InsertRowMsg msg) {
        if (seenTransaction(msg)) return;
        Row newRow = msg.getRow();

        if (isFull) {
            // Can't insert new rows while the partition is being split
            BlockedRow blockedRow = new BlockedRow(newRow, msg.getQueryMetaInfo());
            table.tell(new PartitionBlockedMsg(blockedRow), getSelf());
            return;
        }

        long newHashKey = newRow.getHashKey();
        assert range.contains(newHashKey);

        StoredRow previousEntry = rows.get(newHashKey);
        if (previousEntry != null) {
            LamportId oldLamportId = previousEntry.getLamportId();

            // We have a newer version stored already (last-write-wins)
            if (oldLamportId.isGreaterThan(msg.getLamportId())) {
                // TODO: is this correct
                String error = "Newer version of key '" + newRow.getKey() + "' exists (" + oldLamportId + ")";
                getSender().tell(new QueryErrorMsg(error, msg.getQueryMetaInfo()), getSelf());
                return;
            }
        }

        rows.put(newHashKey, new StoredRow(newRow, msg.getLamportId()));
        log.debug("({}) Added row: {}", msg.getLamportId(), newRow);

        if (rows.size() == CAPACITY) {
            isFull = true;
            List<Long> allRows = new ArrayList<>(rows.keySet());
            allRows.sort(Comparator.naturalOrder());

            long medianKey = allRows.get(CAPACITY / 2);
            long lowestInNewPartition = rows.get(medianKey).getRow().getHashKey();

            Range<Long> newRange = Range.closed(lowestInNewPartition, range.upperEndpoint());

            // We want to cover all values up to the lowest in the new partition
            range = Range.closed(range.lowerEndpoint(), lowestInNewPartition - 1);

            table.tell(new PartitionFullMsg(newRange), getSelf());
        }

        getSender().tell(new QuerySuccessMsg(msg.getQueryMetaInfo()), getSelf());
    }

    private void handleSplitPartition(SplitPartitionMsg msg) {
        ActorRef other = msg.getNewPartition();
        List<StoredRow> sorted = getSortedStoredRows();
        List<StoredRow> rowsToCopy = new ArrayList<>(sorted.subList(CAPACITY / 2, CAPACITY));
        other.tell(new SplitInsertMsg(rowsToCopy), getSelf());
    }

    private void handleSplitInsert(SplitInsertMsg msg) {
        rows = msg.getRows().stream().collect(Collectors.toMap(sr -> sr.getRow().getHashKey(), sr -> sr));
        log.debug("Inserted {} new rows from split", rows.size());
        getSender().tell(new PartialSplitSuccessMsg(getSelf(), range), getSelf());
    }

    private void handlePartialSplitSuccess(PartialSplitSuccessMsg msg) {
        // Only keep first half of rows because the rest were successfully moved to new partition
        List<StoredRow> sorted = getSortedStoredRows();
        List<StoredRow> rowsToKeep = new ArrayList<>(sorted.subList(0, CAPACITY / 2));
        rows = rowsToKeep.stream().collect(Collectors.toMap(sr -> sr.getRow().getHashKey(), sr -> sr));
        isFull = false;
        table.tell(new SplitSuccessMsg(msg.getNewPartition(), msg.getNewRange(), getSelf(), range), getSelf());
    }

    private boolean seenTransaction(QueryMsg msg) {
        return !seenTransactions.add(msg.getLamportId());
    }

    private List<StoredRow> getSortedStoredRows() {
        // TODO: performance 'n shit
        return rows.entrySet().stream()
                .map(Map.Entry::getValue)
                .sorted(Comparator.comparingLong(sr -> sr.getRow().getHashKey()))
                .collect(Collectors.toList());
    }

}
