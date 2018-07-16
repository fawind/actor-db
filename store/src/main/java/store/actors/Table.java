package store.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import api.messages.QueryErrorMsg;
import api.messages.QuerySuccessMsg;
import api.model.Row;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import store.messages.partition.PartitionBlockedMsg;
import store.messages.partition.PartitionFullMsg;
import store.messages.partition.SplitPartitionMsg;
import store.messages.partition.SplitSuccessMsg;
import store.messages.query.DeleteKeyMsg;
import store.messages.query.InsertRowMsg;
import store.messages.query.SelectAllMsg;
import store.messages.query.SelectKeyMsg;
import store.messages.query.SelectWhereMsg;
import store.model.BlockedRow;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Table extends AbstractDBActor {
    public static final String ACTOR_NAME = "table";
    private final List<String> layout;

    // Map of Rows that could not be inserted to a partition because of ongoing splitting
    private final Multimap<ActorRef, BlockedRow> blockedRows;

    // Map of all leading partitions and the key ranges they each cover
    private final RangeMap<Long, ActorRef> partitions = TreeRangeMap.create();

    private Table(List<String> layout) {
        this.layout = layout;

        blockedRows = MultimapBuilder.hashKeys().arrayListValues().build();

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
                .match(SelectKeyMsg.class, this::handleSelectKey)
                .match(DeleteKeyMsg.class, this::handleDeleteKey)
                .match(InsertRowMsg.class, this::handleInsert)
                .match(QuerySuccessMsg.class, this::handleQuerySuccess)

                // Partitioning
                .match(PartitionFullMsg.class, this::handlePartitionFull)
                .match(PartitionBlockedMsg.class, this::handlePartitionBlocked)
                .match(SplitSuccessMsg.class, this::handleSplitSuccess)

                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }

    private void handleSelectAll(SelectAllMsg msg) {
        ActorRef resultCollector = startMultiPartitionQuery();
        broadcastToPartitions(msg, resultCollector);
    }

    private void handleSelectWhere(SelectWhereMsg msg) {
        ActorRef resultCollector = startMultiPartitionQuery();
        broadcastToPartitions(msg, resultCollector);
    }

    private void handleSelectKey(SelectKeyMsg msg) {
        long hashKey = Row.hash(msg.getKey());
        ActorRef partition = partitions.get(hashKey);
        partition.tell(msg, getSender());
    }

    private void handleDeleteKey(DeleteKeyMsg msg) {
        long hashKey = Row.hash(msg.getKey());
        ActorRef partition = partitions.get(hashKey);
        partition.tell(msg, getSender());
    }

    private void handleInsert(InsertRowMsg msg) {
        if (msg.getRow().getValues().size() != layout.size()) {
            msg.getRequester().tell(new QueryErrorMsg("Insert mismatch! Expected " + layout.size() + " columns but " +
                    "got " +
                    msg.getRow().getValues().size(), msg.getQueryMetaInfo()), getSelf());
        }
        ActorRef partition = partitions.get(msg.getRow().getHashKey());
        partition.tell(msg, getSender());
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

    private ActorRef startMultiPartitionQuery() {
        return getContext().actorOf(TableResponseCollector.props(getSender(), createCurrentPartitionSet()),
                TableResponseCollector.ACTOR_NAME + "-" + (int)(Math.random() * 1000000));
    }

    private ActorRef createPartition(Range<Long> range) {
        log.debug("Created new leader partition with range: {}", range);
        ActorRef partition = getContext().actorOf(Partition.props(range, getSelf()),
                Partition.ACTOR_NAME + "-" + range.lowerEndpoint().toString());

        partitions.put(range, partition);
        return partition;
    }

    private <MsgType> void broadcastToPartitions(MsgType msg, ActorRef resultCollector) {
        partitions.asMapOfRanges().forEach((range, partition) -> partition.tell(msg, resultCollector));
    }

    private Set<ActorRef> createCurrentPartitionSet() {
        return new HashSet<>(partitions.asMapOfRanges().values());
    }
}
