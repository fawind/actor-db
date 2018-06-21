package store.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import store.messages.query.PartialQueryResultMsg;
import store.model.StoredRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TableResponseCollector extends AbstractDBActor {

    private final ActorRef quorumManager;
    private final Set<ActorRef> queriedPartitions;

    private final List<StoredRow> runningQueryResults;

    private TableResponseCollector(ActorRef quorumManager, Set<ActorRef> queriedPartitions) {
        this.quorumManager = quorumManager;
        this.queriedPartitions = queriedPartitions;

        runningQueryResults = new ArrayList<>();
    }

    static Props props(ActorRef quorumManager, Set<ActorRef> queriedPartitions) {
        return Props.create(TableResponseCollector.class, () -> new TableResponseCollector(quorumManager, queriedPartitions));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PartialQueryResultMsg.class, this::handlePartialQueryResult)
                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }

    private void handlePartialQueryResult(PartialQueryResultMsg msg) {
        runningQueryResults.addAll(msg.getResult());

        queriedPartitions.remove(getSender());

        if (!queriedPartitions.isEmpty()) return;

        // Seen all results, pass result to quorumManager
        quorumManager.tell(new PartialQueryResultMsg(runningQueryResults, msg.getQueryMetaInfo()), ActorRef.noSender());

        // The collector has finished its job so it can be destroyed
        getContext().stop(getSelf());
    }
}
