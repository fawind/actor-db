package store.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import api.messages.LamportId;
import api.messages.QueryErrorMsg;
import api.messages.QueryResponseMsg;
import api.messages.QuerySuccessMsg;
import api.model.Row;
import api.model.TombstoneRow;
import com.google.common.collect.Range;
import store.messages.partition.PartialSplitSuccessMsg;
import store.messages.partition.PartitionBlockedMsg;
import store.messages.partition.PartitionFullMsg;
import store.messages.partition.SplitInsertMsg;
import store.messages.partition.SplitPartitionMsg;
import store.messages.partition.SplitSuccessMsg;
import store.messages.query.DeleteKeyMsg;
import store.messages.query.InsertRowMsg;
import store.messages.query.PartialQueryResultMsg;
import store.messages.query.SelectAllMsg;
import store.messages.query.SelectKeyMsg;
import store.messages.query.SelectWhereMsg;
import store.model.BlockedRow;
import store.model.StoredRow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Partition extends AbstractDBActor {

    private final int CAPACITY = 100;

    private final ActorRef table;

    private boolean isFull = false;

    private Range<Long> range;
    private Map<Long, StoredRow> rows;


    private Partition(Range<Long> startRange, ActorRef table) {
        this.range = startRange;
        this.table = table;
        this.rows = new HashMap<>(CAPACITY);
    }

    public static Props props(Range<Long> startRange, ActorRef table) {
        return Props.create(Partition.class, () -> new Partition(startRange, table));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // Querying
                .match(SelectAllMsg.class, this::handleSelectAll)
                .match(SelectWhereMsg.class, this::handleSelectWhere)
                .match(SelectKeyMsg.class, this::handleSelectKey)
                .match(DeleteKeyMsg.class, this::handleDeleteKey)
                .match(InsertRowMsg.class, this::handleInsert)

                // Partitioning
                .match(SplitPartitionMsg.class, this::handleSplitPartition)
                .match(SplitInsertMsg.class, this::handleSplitInsert)
                .match(PartialSplitSuccessMsg.class, this::handlePartialSplitSuccess)

                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }

    private void handleSelectAll(SelectAllMsg msg) {
        List<StoredRow> resultRows = new ArrayList<>(rows.values());
        getSender().tell(new PartialQueryResultMsg(resultRows, msg.getQueryMetaInfo()), getSelf());
    }

    private void handleSelectWhere(SelectWhereMsg msg) {
        Predicate<Row> whereFn = msg.getWhereFn();
        List<StoredRow> resultRows = rows.values().stream()
                .filter(storedRow -> whereFn.test(storedRow.getRow()))
                .collect(Collectors.toList());
        getSender().tell(new PartialQueryResultMsg(resultRows, msg.getQueryMetaInfo()), getSelf());
    }

    private void handleSelectKey(SelectKeyMsg msg) {
        long hashKey = Row.hash(msg.getKey());
        StoredRow result = rows.get(hashKey);

        List<StoredRow> resultRows = new ArrayList<>(1);
        if (result != null) {
            resultRows.add(result);
        }

        getSender().tell(new PartialQueryResultMsg(resultRows, msg.getQueryMetaInfo()), getSelf());
    }

    private void handleDeleteKey(DeleteKeyMsg msg) {
        String rowKey = msg.getKey();
        long hashKey = Row.hash(rowKey);
        StoredRow previousEntry = rows.get(hashKey);

        QueryResponseMsg response;
        if (previousEntry != null) {
            LamportId oldLamportId = previousEntry.getLamportId();

            // We have a newer version stored already (last-write-wins)
            if (oldLamportId.isGreaterThan(msg.getLamportId())) {
                String error = "Newer version of key '" + rowKey + "' exists (" + oldLamportId + ")";
                response = new QueryErrorMsg(error, msg.getQueryMetaInfo());
            } else {
                StoredRow tombstone = new StoredRow(new TombstoneRow(rowKey), msg.getLamportId());
                rows.put(hashKey, tombstone);
                response = new QuerySuccessMsg(msg.getQueryMetaInfo());
            }
        } else {
            // Key doesn't exist
            response = new QueryErrorMsg("No such key to delete: " + rowKey, msg.getQueryMetaInfo());
        }

        getSender().tell(response, getSelf());
    }

    private void handleInsert(InsertRowMsg msg) {
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
        for (StoredRow sr : msg.getRows()) {
            rows.put(sr.getRow().getHashKey(), sr);
        }
        log.debug("Inserted {} new rows from split", msg.getRows().size());
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

    private List<StoredRow> getSortedStoredRows() {
        // TODO: performance 'n shit
        return rows.entrySet().stream()
                .map(Map.Entry::getValue)
                .sorted(Comparator.comparingLong(sr -> sr.getRow().getHashKey()))
                .collect(Collectors.toList());
    }

}
