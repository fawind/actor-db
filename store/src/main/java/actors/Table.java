package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import api.messages.LamportId;
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
import messages.query.QuerySuccessMsg;
import messages.query.SelectAllMsg;
import messages.query.SelectWhereMsg;
import model.BlockedRow;
import model.StoredRow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Table extends AbstractDBActor {
    private final List<String> layout;

    // Map of Rows that could not be inserted to a partition because of ongoing splitting
    private final Multimap<ActorRef, BlockedRow> blockedRows;

    // Maps of current transactions (by ID) to the actors which have not responded and to the accumulated result
    private final Map<LamportId, Set<ActorRef>> runningQueries;
    private final Multimap<LamportId, StoredRow> runningQueryResults;
    private final Map<LamportId, ActorRef> queryQuorumManagers;

    // Map of all leading partitions and the key ranges they each cover
    private final RangeMap<Long, ActorRef> partitions = TreeRangeMap.create();


    // Counter to give unique ids to each partition
    private int highestPartitionId = 1;

    private Table(List<String> layout) {
        this.layout = layout;

        blockedRows = MultimapBuilder.hashKeys().arrayListValues().build();
        runningQueries = new HashMap<>();
        runningQueryResults = MultimapBuilder.hashKeys().arrayListValues().build();
        queryQuorumManagers = new HashMap<>();

        Range<Long> startRange = Range.closed(Long.MIN_VALUE, Long.MAX_VALUE);
        createPartition(startRange);
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
        startTransaction(msg.getLamportId());
        broadcastToPartitions(msg);
    }

    private void handleSelectWhere(SelectWhereMsg msg) {
        startTransaction(msg.getLamportId());
        broadcastToPartitions(msg);
    }

    private void handleInsert(InsertRowMsg msg) {
        if (msg.getRow().getValues().size() != layout.size()) {
            msg.getRequester().tell(new QueryErrorMsg("Insert mismatch! Expected " + layout.size() + " columns but got " +
                    msg.getRow().getValues().size(), msg.getQueryMetaInfo()), getSelf());
        }
        ActorRef partition = partitions.get(msg.getRow().getHashKey());
        partition.tell(msg, getSender());
    }

    private void handlePartialQueryResult(PartialQueryResultMsg msg) {
        LamportId lamportId = msg.getLamportId();

        runningQueryResults.putAll(lamportId, msg.getResult());
        updateTransaction(lamportId, getSender());

        if (!isTransactionDone(lamportId)) return;

        List<StoredRow> result = new ArrayList<>(runningQueryResults.get(lamportId));
        ActorRef quorumManager = queryQuorumManagers.get(lamportId);
        log.debug("Telling QM: {}", lamportId);
        quorumManager.tell(new PartialQueryResultMsg(result, msg.getQueryMetaInfo()), getSelf());
        finishTransaction(lamportId);
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
            partition.tell(new InsertRowMsg(blockedRow.getRow(), blockedRow.getQueryMetaInfo()), getSelf());
        }

        blockedRows.clear();
    }

    private void handleQuerySuccess(QuerySuccessMsg msg) {
        msg.getRequester().tell(msg, getSelf());
    }

    private void startTransaction(LamportId lamportId) {
        runningQueries.put(lamportId, createCurrentPartitionSet());
        queryQuorumManagers.put(lamportId, getSender());
    }

    private void updateTransaction(LamportId lamportId, ActorRef actor) {
        Set<ActorRef> partitionSet = runningQueries.get(lamportId);
        if (partitionSet == null) {
            log.error("Cannot update queryMetaInfo #{}. It is not present in list of transactions", lamportId);
            return;
        }
        partitionSet.remove(actor);
    }

    private boolean isTransactionDone(LamportId lamportId) {
        Set<ActorRef> partitionSet = runningQueries.get(lamportId);
        return partitionSet != null && partitionSet.isEmpty();
    }

    private void finishTransaction(LamportId lamportId) {
        runningQueries.remove(lamportId);
        runningQueryResults.removeAll(lamportId);
        queryQuorumManagers.remove(lamportId);
    }

    private ActorRef createPartition(Range<Long> range) {
        int partitionId = getNextPartitionId();
        log.debug("Created new leader partition #{} with range: {}", partitionId, range);
        ActorRef partition = getContext().actorOf(Partition.props(partitionId, range, getSelf()), "partition-" +
                partitionId);

        partitions.put(range, partition);
        return partition;
    }

    private int getNextPartitionId() {
        return highestPartitionId++;
    }

    private <MsgType> void broadcastToPartitions(MsgType msg) {
        partitions.asMapOfRanges().forEach((range, partition) -> partition.tell(msg, getSelf()));
    }

    private Set<ActorRef> createCurrentPartitionSet() {
        return new HashSet<>(partitions.asMapOfRanges().values());
    }
}
