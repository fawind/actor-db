package core;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import messages.InsertRowMsg;
import messages.PartialQueryResultMsg;
import messages.PartitionBlockedMsg;
import messages.PartitionFullMsg;
import messages.QueryResultMsg;
import messages.QuerySuccessMsg;
import messages.SelectAllMsg;
import messages.SelectWhereMsg;
import messages.SplitPartitionMsg;
import messages.SplitSuccessMsg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Table extends AbstractDBActor {
    private final String layout;
    private int highestPartitionId = 1;

    private final Multimap<ActorRef, BlockedRow> blockedRows;
    private final Map<Long, Set<Integer>> runningTransactions;
    private final Multimap<Long, Row> runningTransactionResults;

    private RangeMap<Long, ActorRef> partitions = TreeRangeMap.create();
    private int numPartitions = 0;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SelectAllMsg.class, this::handleSelectAll)
                .match(SelectWhereMsg.class, this::handleSelectWhere)
                .match(InsertRowMsg.class, this::handleInsert)
                .match(PartialQueryResultMsg.class, this::handlePartialQueryResult)
                .match(PartitionFullMsg.class, this::handlePartitionFull)
                .match(PartitionBlockedMsg.class, this::handlePartitionBlocked)
                .match(SplitSuccessMsg.class, this::handleSplitSuccess)
                .match(QuerySuccessMsg.class, this::handleQuerySuccess)
                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }

    static Props props(String layout) {
        return Props.create(Table.class, () -> new Table(layout));
    }

    private Table(String layout) {
        this.layout = layout;

        blockedRows = MultimapBuilder.hashKeys().arrayListValues().build();
        runningTransactions = new HashMap<>();
        runningTransactionResults = MultimapBuilder.hashKeys().arrayListValues().build();

        Range<Long> startRange = Range.closed(Long.MIN_VALUE, Long.MAX_VALUE);
        createPartition(startRange);
    }

    private void handleSelectAll(SelectAllMsg msg) {
        startTransaction(msg.getTransactionId());
        broadcast(msg);
    }

    private void handleSelectWhere(SelectWhereMsg msg) {
        startTransaction(msg.getTransactionId());
        broadcast(msg);
    }

    private void handleInsert(InsertRowMsg msg) {
        ActorRef partition = partitions.get(msg.getRow().getHashKey());
        partition.tell(msg, getSelf());
    }

    private void handlePartialQueryResult(PartialQueryResultMsg msg) {
        long transactionId = msg.getTransactionId();

        runningTransactionResults.putAll(transactionId, msg.getResult());

        updateTransaction(transactionId, msg.getActorId());

        if (isTransactionDone(transactionId)) {
            finishTransaction(transactionId);
            List<Row> result = new ArrayList<>(runningTransactionResults.get(transactionId));
            msg.getRequester().tell(new QueryResultMsg(result, msg.getTransaction()), getSelf());
        }
    }

    private void handlePartitionFull(PartitionFullMsg msg) {
        ActorRef newPartition = createPartition(msg.getNewRange());
        getSender().tell(new SplitPartitionMsg(newPartition), getSelf());
    }

    private void handlePartitionBlocked(PartitionBlockedMsg msg) {
        blockedRows.put(getSender(), msg.getBlockedRow());
    }

    private void handleSplitSuccess(SplitSuccessMsg msg) {
        Range<Long> oldRange = partitions.getEntry(msg.getOldRange().lowerEndpoint()).getKey();
        partitions.remove(oldRange);

        partitions.put(msg.getOldRange(), msg.getOldPartition());
        partitions.put(msg.getNewRange(), msg.getNewPartition());

        Collection<BlockedRow> blockedRows = this.blockedRows.get(msg.getOldPartition());
        for (BlockedRow blockedRow : blockedRows) {
            ActorRef partition = partitions.get(blockedRow.getRow().getHashKey());
            partition.tell(new InsertRowMsg(blockedRow.getRow(), blockedRow.getTransaction()), getSelf());
        }

        blockedRows.clear();
    }

    private void handleQuerySuccess(QuerySuccessMsg msg) {
        msg.getRequester().tell(msg, getSelf());
    }

    private void startTransaction(long transactionId) {
        runningTransactions.put(transactionId, createCurrentPartitionSet());
    }

    private void updateTransaction(long transactionId, int actorId) {
        Set<Integer> partitionSet = runningTransactions.get(transactionId);
        if (partitionSet == null) {
            log.error("Cannot update transaction #{}. It is not present in list of transactions", transactionId);
            return;
        }

        partitionSet.remove(actorId);
    }

    private boolean isTransactionDone(long transactionId) {
        Set<Integer> partitionSet = runningTransactions.get(transactionId);
        return partitionSet != null && partitionSet.isEmpty();
    }

    private void finishTransaction(long transactionId) {
        runningTransactions.remove(transactionId);
    }

    private ActorRef createPartition(Range<Long> range) {
        int partitionId = highestPartitionId++;
        log.info("Created new partition #{} with range: {}", partitionId, range);
        ActorRef partition = getContext().actorOf(Partition.props(partitionId, range, getSelf()), "partition-" + partitionId);
        partitions.put(range, partition);
        numPartitions++;
        return partition;
    }

    private <MsgType> void broadcast(MsgType msg) {
        partitions.asMapOfRanges().forEach(
                (range, partition) -> partition.tell(msg, getSelf())
        );
    }

    private Set<Integer> createCurrentPartitionSet() {
        Set<Integer> partitionSet = new HashSet<>(numPartitions);
        for (int i = 1; i <= numPartitions; ++i) {
            partitionSet.add(i);
        }

        return partitionSet;
    }
}
