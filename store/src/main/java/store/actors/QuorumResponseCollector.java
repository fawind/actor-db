package store.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import api.messages.LamportId;
import api.messages.QueryErrorMsg;
import api.messages.QueryMetaInfo;
import api.messages.QueryMsg;
import api.messages.QueryResponseMsg;
import api.messages.QueryResultMsg;
import api.messages.QuerySuccessMsg;
import api.model.Row;
import api.model.TombstoneRow;
import store.messages.query.PartialQueryResultMsg;
import store.model.StoredRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuorumResponseCollector extends AbstractDBActor {

    public static final String ACTOR_NAME = "quorum-response-collector";
    private final ActorRef client;
    private final int quorumSize;

    private final List<QueryResponseMsg> quorumResponses;

    private LamportId newestLamportId = LamportId.INVALID_LAMPORT_ID;

    public QuorumResponseCollector(ActorRef client, int quorumSize) {
        this.client = client;
        this.quorumSize = quorumSize;

        quorumResponses = new ArrayList<>();
    }

    static Props props(ActorRef client, int quorumSize) {
        return Props.create(QuorumResponseCollector.class, () -> new QuorumResponseCollector(client, quorumSize));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(QueryResponseMsg.class, this::handleQueryResponseMsg)
                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }

    private void handleQueryResponseMsg(QueryResponseMsg msg) {
        newestLamportId = newestLamportId.max(msg.getLamportId());

        // For now, if we see an error we assume everything is an error
        if (msg instanceof QueryErrorMsg) {
            tellWithNewestResponseLamportId(msg);
            getContext().stop(getSelf());
            return;
        }

        quorumResponses.add(msg);

        if (quorumResponses.size() < quorumSize) return;

        // Seen all results, pass result to quorumManager
        QueryResponseMsg quorumResponse = getQuorumResponse();
        tellWithNewestResponseLamportId(quorumResponse);

        // The collector has finished its job so it can be destroyed
        getContext().stop(getSelf());
    }

    private QueryResponseMsg getQuorumResponse() {
        QueryMetaInfo queryMetaInfo = quorumResponses.get(0).getQueryMetaInfo();
        LamportId lampId = queryMetaInfo.getLamportId();
        log.debug("({}) Quorum response", lampId);

        Class<? extends QueryResponseMsg> msgClass = quorumResponses.get(0).getClass();

        if (msgClass == PartialQueryResultMsg.class) {
            return getResultQuorum();
        } else if (msgClass == QuerySuccessMsg.class) {
            return getSuccessQuorum();
        } else {
            throw new RuntimeException("Unknown response type: " + msgClass);
        }
    }

    private QueryResponseMsg getResultQuorum() {
        Map<Long, StoredRow> resultRowsMap = new HashMap<>();

        for (QueryResponseMsg msg : quorumResponses) {
            PartialQueryResultMsg resultMsg = (PartialQueryResultMsg) msg;
            for (StoredRow storedRow : resultMsg.getResult()) {
                long key = storedRow.getRow().getHashKey();
                StoredRow prev = resultRowsMap.get(key);

                // If a newer version is in the result set, ignore this row
                if (prev != null && prev.getLamportId().isGreaterThan(storedRow.getLamportId())) {
                    continue;
                }

                resultRowsMap.put(key, storedRow);
            }
        }

        List<Row> resultRows = resultRowsMap.values().stream()
                .filter(sr -> !(sr.getRow() instanceof TombstoneRow))
                .map(StoredRow::getRow).collect(Collectors.toList());
        return new QueryResultMsg(resultRows, quorumResponses.get(0).getQueryMetaInfo());
    }

    private QueryResponseMsg getSuccessQuorum() {
        // All queries returned success, so we can return any one of them
        return quorumResponses.get(0);
    }

    private void tellWithNewestResponseLamportId(QueryMsg msg) {
        msg.updateMetaInfo(addResponseLamportIdToMeta(msg.getQueryMetaInfo()));
        client.tell(msg, ActorRef.noSender());
    }

    private QueryMetaInfo addResponseLamportIdToMeta(QueryMetaInfo metaInfo) {
        return metaInfo.copyWithResponseLamportId(updatedLamportId(metaInfo.getLamportId()));
    }

    private LamportId updatedLamportId(LamportId other) {
        newestLamportId = newestLamportId.maxIdCopy(other);
        return newestLamportId;
    }
}
