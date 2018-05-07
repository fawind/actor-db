package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import messages.partition.PartitionBlockedMsg;
import messages.partition.PartitionFullMsg;
import messages.partition.SplitPartitionMsg;
import messages.partition.SplitSuccessMsg;
import messages.query.InsertRowMsg;
import messages.query.PartialQueryResultMsg;
import messages.query.QueryErrorMsg;
import messages.query.QueryResultMsg;
import messages.query.QuerySuccessMsg;
import messages.query.SelectAllMsg;
import messages.query.SelectWhereMsg;
import messages.replication.UpdateReplicasMsg;
import model.BlockedRow;
import model.Row;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Table extends AbstractDBActor {

    // How many partitions should hold the same info. Factor e.g. 3 equals 1 leader partition and 2 replica
    private final int PARTITION_REPLICATION_FACTOR = 2;

    private final List<String> layout;

    // Map of Rows that could not be inserted to a partition because of ongoing splitting
    private final Multimap<ActorRef, BlockedRow> blockedRows;

    // Maps of current transactions (by ID) to the actors which have not responded and to the accumulated result
    private final Map<Long, Set<ActorRef>> runningTransactions;
    private final Multimap<Long, Row> runningTransactionResults;

    // Map of all leading partitions and the key ranges they each cover
    private final RangeMap<Long, ActorRef> leaderPartitions = TreeRangeMap.create();

    // List of all partitions, including all replicas
    private final List<ActorRef> allPartitions = new ArrayList<>();

    // Counter to give unique ids to each partition
    private int highestPartitionId = 1;

    private Table(List<String> layout) {
        this.layout = layout;

        blockedRows = MultimapBuilder.hashKeys().arrayListValues().build();
        runningTransactions = new HashMap<>();
        runningTransactionResults = MultimapBuilder.hashKeys().arrayListValues().build();

        Range<Long> startRange = Range.closed(Long.MIN_VALUE, Long.MAX_VALUE);
        createLeaderPartition(startRange);
    }

    public static Props props(List<String> layout) {
        return Props.create(Table.class, () -> new Table(layout));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // Querying
                .match(SelectAllMsg.class, this::handleSelectAll)
                .match(SelectWhereMsg.class, this::handleSelectWhere)
                .match(InsertRowMsg.class, this::handleInsert)
                .match(QuerySuccessMsg.class, this::handleQuerySuccess)

                // Partitioning
                .match(PartialQueryResultMsg.class, this::handlePartialQueryResult)
                .match(PartitionFullMsg.class, this::handlePartitionFull)
                .match(PartitionBlockedMsg.class, this::handlePartitionBlocked)
                .match(SplitSuccessMsg.class, this::handleSplitSuccess)

                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }

    private void handleSelectAll(SelectAllMsg msg) {
        startTransaction(msg.getTransactionId());
        broadcastToLeaders(msg);
    }

    private void handleSelectWhere(SelectWhereMsg msg) {
        startTransaction(msg.getTransactionId());
        broadcastToLeaders(msg);
    }

    private void handleInsert(InsertRowMsg msg) {
        if (msg.getRow().getValues().size() != layout.size()) {
            msg.getRequester().tell(new QueryErrorMsg("Insert mismatch! Expected " + layout.size() + "columns but got" +
                    " " + msg.getRow().getValues().size(), msg.getTransaction()), getSelf());
        }
        ActorRef partition = leaderPartitions.get(msg.getRow().getHashKey());
        partition.tell(msg, getSelf());
    }

    private void handlePartialQueryResult(PartialQueryResultMsg msg) {
        long transactionId = msg.getTransactionId();

        runningTransactionResults.putAll(transactionId, msg.getResult());
        updateTransaction(transactionId, getSender());

        if (isTransactionDone(transactionId)) {
            finishTransaction(transactionId);
            List<Row> result = new ArrayList<>(runningTransactionResults.get(transactionId));
            msg.getRequester().tell(new QueryResultMsg(result, msg.getTransaction()), getSelf());
        }
    }

    private void handlePartitionFull(PartitionFullMsg msg) {
        ActorRef newPartition = createLeaderPartition(msg.getNewRange());
        getSender().tell(new SplitPartitionMsg(newPartition), getSelf());
    }

    private void handlePartitionBlocked(PartitionBlockedMsg msg) {
        blockedRows.put(getSender(), msg.getBlockedRow());
    }

    private void handleSplitSuccess(SplitSuccessMsg msg) {
        Range<Long> oldRange = leaderPartitions.getEntry(msg.getOldRange().lowerEndpoint()).getKey();
        leaderPartitions.remove(oldRange);

        leaderPartitions.put(msg.getOldRange(), msg.getOldPartition());
        leaderPartitions.put(msg.getNewRange(), msg.getNewPartition());

        Collection<BlockedRow> blockedRows = this.blockedRows.get(msg.getOldPartition());
        for (BlockedRow blockedRow : blockedRows) {
            ActorRef partition = leaderPartitions.get(blockedRow.getRow().getHashKey());
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

    private void updateTransaction(long transactionId, ActorRef actor) {
        Set<ActorRef> partitionSet = runningTransactions.get(transactionId);
        if (partitionSet == null) {
            log.error("Cannot update transaction #{}. It is not present in list of transactions", transactionId);
            return;
        }
        partitionSet.remove(actor);
    }

    private boolean isTransactionDone(long transactionId) {
        Set<ActorRef> partitionSet = runningTransactions.get(transactionId);
        return partitionSet != null && partitionSet.isEmpty();
    }

    private void finishTransaction(long transactionId) {
        runningTransactions.remove(transactionId);
    }

    private ActorRef createLeaderPartition(Range<Long> range) {
        int partitionId = getNextPartitionId();
        log.debug("Created new leader partition #{} with range: {}", partitionId, range);
        ActorRef partition = getContext().actorOf(Partition.props(partitionId, range, getSelf()), "partition-" +
                partitionId);

        leaderPartitions.put(range, partition);
        allPartitions.add(partition);

        List<ActorRef> replicas = createReplicas(range);
        allPartitions.addAll(replicas);

        partition.tell(new UpdateReplicasMsg(replicas), getSelf());

        return partition;
    }

    private List<ActorRef> createReplicas(Range<Long> range) {
        List<ActorRef> replicas = new ArrayList<>(PARTITION_REPLICATION_FACTOR);

        // The first one is the leader partition, so we start at index 1
        for (int i = 1; i < PARTITION_REPLICATION_FACTOR; i++) {
            int partitionId = getNextPartitionId();
            log.debug("Created new replica partition #{} for range: {}", partitionId, range);
            ActorRef partition = getContext().actorOf(Partition.props(partitionId, range, getSelf()), "partition-" +
                    partitionId);
            replicas.add(partition);
        }

        return replicas;
    }

    private int getNextPartitionId() {
        return highestPartitionId++;
    }

    private <MsgType> void broadcastToLeaders(MsgType msg) {
        leaderPartitions.asMapOfRanges().forEach((range, partition) -> partition.tell(msg, getSelf()));
    }

    private Set<ActorRef> createCurrentPartitionSet() {
        return new HashSet<>(leaderPartitions.asMapOfRanges().values());
    }
}