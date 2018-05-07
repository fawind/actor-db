package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.Scheduler;
import com.google.common.collect.Range;
import messages.partition.PartialSplitSuccessMsg;
import messages.partition.PartitionBlockedMsg;
import messages.partition.PartitionFullMsg;
import messages.partition.SplitInsertMsg;
import messages.partition.SplitPartitionMsg;
import messages.partition.SplitSuccessMsg;
import messages.query.InsertRowMsg;
import messages.query.PartialQueryResultMsg;
import messages.query.QuerySuccessMsg;
import messages.query.SelectAllMsg;
import messages.query.SelectWhereMsg;
import messages.query.TransactionMsg;
import messages.replication.ReplicateAckMsg;
import messages.replication.ReplicateMsg;
import messages.replication.UpdateReplicasMsg;
import model.BlockedRow;
import model.Row;
import utils.FIFOCache;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Partition extends AbstractDBActor {

    private final int CAPACITY = 10;

    private final int partitionId;
    private final ActorRef table;
    private final Map<Long, Map<ActorRef, Cancellable>> openTransactionAcks = new HashMap<>();
    private final Set<Long> seenTransactions = FIFOCache.newSet(1024);
    private boolean isFull = false;
    private Range<Long> range;
    private List<Row> rows;

    private List<ActorRef> replicas = new ArrayList<>();

    private Partition(int partitionId, Range<Long> startRange, ActorRef table) {
        this.partitionId = partitionId;
        this.range = startRange;
        this.table = table;
        this.rows = new ArrayList<>(CAPACITY);
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

                // Replicating
                .match(ReplicateMsg.class, this::handleReplicate)
                .match(ReplicateAckMsg.class, this::handleReplicateAck)
                .match(UpdateReplicasMsg.class, this::handleUpdateReplicas)

                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }

    private void handleSelectAll(SelectAllMsg msg) {
        if (!insertNewTransaction(msg)) return;

        List<Row> resultRows = new ArrayList<>(rows);
        getSender().tell(new PartialQueryResultMsg(resultRows, partitionId, msg.getTransaction()), getSelf());
    }

    private void handleSelectWhere(SelectWhereMsg msg) {
        if (!insertNewTransaction(msg)) return;

        Predicate<Row> whereFn = msg.getWhereFn();
        List<Row> resultRows = rows.stream().filter(whereFn).collect(Collectors.toList());
        getSender().tell(new PartialQueryResultMsg(resultRows, partitionId, msg.getTransaction()), getSelf());
    }

    private void handleInsert(InsertRowMsg msg) {
        if (!insertNewTransaction(msg)) return;

        if (isFull) {
            // Can't insert new rows while the partition is being split
            BlockedRow blockedRow = new BlockedRow(msg.getRow(), msg.getTransaction());
            getSender().tell(new PartitionBlockedMsg(blockedRow), getSelf());
            return;
        }

        assert range.contains(msg.getRow().getHashKey());

        rows.add(msg.getRow());
        log.debug("(TID: {}) Added row: {}", msg.getTransactionId(), msg.getRow());

        broadcastToReplicasWithRetry(new ReplicateMsg(msg.getRow(), msg.getTransaction()));

        if (rows.size() == CAPACITY) {
            isFull = true;
            rows.sort(Comparator.naturalOrder());

            long lowestInNewPartition = rows.get(CAPACITY / 2).getHashKey();

            Range<Long> newRange = Range.closed(lowestInNewPartition, range.upperEndpoint());

            // We want to cover all values up to the lowest in the new partition
            range = Range.closed(range.lowerEndpoint(), lowestInNewPartition - 1);

            getSender().tell(new PartitionFullMsg(newRange), getSelf());
        }

        msg.getRequester().tell(new QuerySuccessMsg(msg.getTransaction()), getSelf());
    }

    private void handleSplitPartition(SplitPartitionMsg msg) {
        ActorRef other = msg.getNewPartition();
        List<Row> rowsToCopy = new ArrayList<>(rows.subList(CAPACITY / 2, CAPACITY));
        other.tell(new SplitInsertMsg(rowsToCopy), getSelf());
    }

    private void handleSplitInsert(SplitInsertMsg msg) {
        rows = msg.getRows();
        log.info("Inserted {} new rows from split", rows.size());
        getSender().tell(new PartialSplitSuccessMsg(getSelf(), range), getSelf());
    }

    private void handlePartialSplitSuccess(PartialSplitSuccessMsg msg) {
        // Only keep first half of rows because the rest were successfully moved to new partition
        rows = new ArrayList<>(rows.subList(0, CAPACITY / 2));
        isFull = false;
        table.tell(new SplitSuccessMsg(msg.getNewPartition(), msg.getNewRange(), getSelf(), range), getSelf());
    }

    private void handleReplicate(ReplicateMsg msg) {
        if (!insertNewTransaction(msg)) return;

//        assert rows.size() < capacity;
        rows.add(msg.getRow());
        log.debug("(TID: {}) Replicated row: {}", msg.getTransactionId(), msg.getRow());
        getSender().tell(new ReplicateAckMsg(msg.getTransaction()), getSelf());
    }

    private void handleUpdateReplicas(UpdateReplicasMsg msg) {
        replicas = msg.getReplicas();
    }

    private void handleReplicateAck(ReplicateAckMsg msg) {
        Map<ActorRef, Cancellable> acks = openTransactionAcks.get(msg.getTransactionId());
        if (acks == null) {
            return;
        }

        Cancellable cancellable = acks.remove(getSender());
        if (cancellable != null) {
            cancellable.cancel();
        }

        if (acks.isEmpty()) {
            openTransactionAcks.remove(msg.getTransactionId());
            log.debug("Completed replication for transaction #{}", msg.getTransactionId());
        }
    }

    private <MsgType extends TransactionMsg> void broadcastToReplicasWithRetry(MsgType msg) {
        long transactionId = msg.getTransactionId();
        Map<ActorRef, Cancellable> acks = new TreeMap<>();
        openTransactionAcks.put(transactionId, acks);

        ActorSystem system = getContext().getSystem();
        Scheduler scheduler = system.scheduler();

        for (ActorRef replica : replicas) {
            Cancellable c = scheduler.schedule(Duration.ZERO, Duration.ofSeconds(10), replica, msg, system.dispatcher(), getSelf());
            acks.put(replica, c);
        }
    }

    private boolean insertNewTransaction(TransactionMsg msg) {
        return seenTransactions.add(msg.getTransactionId());
    }

}
