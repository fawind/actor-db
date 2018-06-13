package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import api.messages.LamportId;
import api.messages.QueryMetaInfo;
import messages.query.PartialQueryResultMsg;
import messages.query.QueryErrorMsg;
import messages.query.QueryMsg;
import messages.query.QueryResponseMsg;
import messages.query.QueryResultMsg;
import messages.query.QuerySuccessMsg;
import model.Row;
import model.StoredRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This class deals with all quorum-related matters. It is aware of all master nodes in the network.
 */
public class QuorumManager extends AbstractDBActor {

    public static final String ACTOR_NAME = "quorum-manager";
    private static final int READ_QUORUM = 1;
    private static final int WRITE_QUORUM = 1;

    private final String id = UUID.randomUUID().toString();
    private final ClusterMemberRegistry memberRegistry;
    private final Map<LamportId, List<QueryResponseMsg>> quorumResponses;
    private LamportId lamportId = new LamportId(id);

    private QuorumManager() {
        memberRegistry = new ClusterMemberRegistry();
        quorumResponses = new HashMap<>();
        getContext().actorOf(ClusterMemberListener.props(memberRegistry), ClusterMemberListener.ACTOR_NAME);
    }

    public static Props props() {
        return Props.create(QuorumManager.class, QuorumManager::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(QueryResponseMsg.class, this::handleQuorumResponse)
                .match(QueryMsg.class, this::handleQuorum)
                .build();
    }

    private void handleQuorumResponse(QueryResponseMsg msg) {
        LamportId lampId = msg.getLamportId();
        List<QueryResponseMsg> responses = quorumResponses.get(lampId);

        // We have dealt with the quorum, the response can be ignored
        if (responses == null) return;

        responses.add(msg);

        int requiredQuorumSize = msg.getQueryMetaInfo().isWriteQuery() ? WRITE_QUORUM : READ_QUORUM;

        // Haven't seen enough responses
        if (responses.size() < requiredQuorumSize) return;

        QueryResponseMsg quorumResponse = getQuorumResponse(responses);
        msg.getRequester().tell(quorumResponse, ActorRef.noSender());

        // Drop quorum responses, as we have seen enough and replied
        quorumResponses.remove(lampId);
    }

    private void handleQuorum(QueryMsg msg) {
        QueryMetaInfo meta = addNewLamportId(msg.getQueryMetaInfo());
        msg.updateMetaInfo(meta);

        LamportId lampId = msg.getLamportId();
        List<QueryResponseMsg> responses = quorumResponses.put(lampId, new ArrayList<>());

        if (responses != null) {
            // We have seen this queryMetaInfo before and can ignore it
            quorumResponses.put(lampId, responses);
            return;
        }
        memberRegistry.getMasters()
                .forEach(actorPath -> getContext().actorSelection(actorPath).tell(msg, getSelf()));
    }

    private QueryResponseMsg getQuorumResponse(List<QueryResponseMsg> messages) {
        QueryMetaInfo queryMetaInfo = messages.get(0).getQueryMetaInfo();
        LamportId lampId = queryMetaInfo.getLamportId();
        log.debug("({}) Quorum response", lampId);

        // TODO: ugly code
        Class<? extends QueryResponseMsg> msgClass = messages.get(0).getClass();
        boolean allResponsesSameType = true;
        QueryErrorMsg encounteredError = null;

        for (QueryResponseMsg msg : messages) {
            allResponsesSameType &= msg.getClass() == msgClass;
            if (msg.getClass() == QueryErrorMsg.class) {
                encounteredError = (QueryErrorMsg) msg;
            }
        }

        if (!allResponsesSameType) {
            // If we have different types and no error, something is really wrong
            assert encounteredError != null;

            // We encountered an error, because they are not all errors and at least one is not of typ Success/Result
            return new QueryErrorMsg("At least one node returned an error response: " + encounteredError.getMsg(),
                    queryMetaInfo);
        }

        if (msgClass == PartialQueryResultMsg.class) {
            return getResultQuorum(messages);
        } else if (msgClass == QuerySuccessMsg.class) {
            return getSuccessQuorum(messages);
        } else if (msgClass == QueryErrorMsg.class) {
            return getErrorQuorum(messages);
        } else {
            throw new RuntimeException("Unknown queryMetaInfo response");
        }
    }

    private QueryResponseMsg getResultQuorum(List<QueryResponseMsg> messages) {
        // TODO: compare messages to get correct result
        Map<Long, StoredRow> resultRowsMap = new HashMap<>();

        for (QueryResponseMsg msg : messages) {
            PartialQueryResultMsg resultMsg = (PartialQueryResultMsg) msg;
            for (StoredRow storedRow : resultMsg.getResult()) {
                // TODO: This will overwrite if a row is present in two different versions
                resultRowsMap.put(storedRow.getRow().getHashKey(), storedRow);
            }
        }

        List<Row> resultRows = resultRowsMap.values().stream().map(StoredRow::getRow).collect(Collectors.toList());
        return new QueryResultMsg(resultRows, messages.get(0).getQueryMetaInfo());
    }

    private QueryResponseMsg getSuccessQuorum(List<QueryResponseMsg> messages) {
        // All queries returned success, so we can return any one of them
        return messages.get(0);
    }

    private QueryResponseMsg getErrorQuorum(List<QueryResponseMsg> messages) {
        // All queries returned an error, so we can return any one of them
        return messages.get(0);
    }

    private QueryMetaInfo addNewLamportId(QueryMetaInfo queryMetaInfo) {
        return queryMetaInfo.copyWithUpdatedLamportId(updatedLamportId(queryMetaInfo.getLamportId()));
    }

    private LamportId updatedLamportId(LamportId other) {
        lamportId = lamportId.maxIdCopy(other);
        return lamportId;
    }
}
