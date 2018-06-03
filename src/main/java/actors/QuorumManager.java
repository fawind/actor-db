package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import messages.query.QueryErrorMsg;
import messages.query.QueryResponseMsg;
import messages.query.QueryResultMsg;
import messages.query.QuerySuccessMsg;
import messages.query.TransactionMsg;
import model.Row;
import model.Transaction;
import model.WriteTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class deals with all quorum-related matters. It is aware of all master nodes in the network.
 */
public class QuorumManager extends AbstractDBActor {

    private static final int READ_QUORUM = 2;
    private static final int WRITE_QUORUM = 2;

    public static Props props() {
        return props(new HashSet<>());
    }

    public static Props props(Set<ActorRef> masters) {
        return Props.create(QuorumManager.class, () -> new QuorumManager(masters));
    }

    private final Set<ActorRef> masters;
    private final Map<Long, List<QueryResponseMsg>> quorumResponses;

    private QuorumManager(Set<ActorRef> masters) {
        this.masters = masters;
        quorumResponses = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(QueryResponseMsg.class, this::handleQuorumResponse)
                .match(TransactionMsg.class, this::handleQuorum)
                .build();
    }

    private void handleQuorumResponse(QueryResponseMsg msg) {
        List<QueryResponseMsg> responses = quorumResponses.get(msg.getTransactionId());

        // We have dealt with the quorum, the response can be ignored
        if (responses == null) return;

        responses.add(msg);

        boolean isWriteTransaction = (msg.getTransaction() instanceof WriteTransaction);
        int requiredQuorumSize = isWriteTransaction ? WRITE_QUORUM : READ_QUORUM;

        // Haven't seen enough responses
        if (responses.size() < requiredQuorumSize) return;

        QueryResponseMsg quorumResponse = getQuorumResponse(responses);
        msg.getRequester().tell(quorumResponse, ActorRef.noSender());

        // Drop quorum responses, as we have seen enough and replied
        quorumResponses.remove(msg.getTransactionId());
    }

    private void handleQuorum(TransactionMsg msg) {
        List<QueryResponseMsg> responses = quorumResponses.put(msg.getTransactionId(), new ArrayList<>());

        if (responses != null) {
            // We have seen this transaction before and can ignore it
            quorumResponses.put(msg.getTransactionId(), responses);
            return;
        }

        masters.forEach(master -> master.tell(msg, getSelf()));
    }

    private QueryResponseMsg getQuorumResponse(List<QueryResponseMsg> messages) {
        Transaction transaction = messages.get(0).getTransaction();
        long transactionId = transaction.getTransactionId();
        log.debug("Quorum for TID {}", transactionId);

        Class<? extends QueryResponseMsg> msgClass = messages.get(0).getClass();
        boolean allResponsesSameType = true;
        QueryErrorMsg encounteredError = null;

        for (QueryResponseMsg msg : messages) {
            allResponsesSameType &= msg.getClass() == msgClass;
            if (msg.getClass() == QueryErrorMsg.class) {
                encounteredError = (QueryErrorMsg) msg;
            }
            assert msg.getTransactionId() == transactionId;
        }

        if (!allResponsesSameType) {
            // If we have different types and no error, something is really wrong
            assert encounteredError != null;

            // We encountered an error, because they are not all errors and at least one is not of typ Success/Result
            return new QueryErrorMsg("At least one node returned an error response: " + encounteredError.getMsg(),
                    transaction);
        }

        if (msgClass == QueryResultMsg.class) {
            return getResultQuorum(messages);
        } else if (msgClass == QuerySuccessMsg.class) {
            return getSuccessQuorum(messages);
        } else {
            return getErrorQuorum(messages);
        }
    }

    private QueryResponseMsg getResultQuorum(List<QueryResponseMsg> messages) {
        // TODO: compare messages to get correct result
        Map<Long, Row> resultRowsMap = new HashMap<>();

        for (QueryResponseMsg msg : messages) {
            QueryResultMsg resultMsg = (QueryResultMsg) msg;
            for (Row row : resultMsg.getResult()) {
                // TODO: This will overwrite if a row is present in two different versions
                resultRowsMap.put(row.getHashKey(), row);
            }
        }

        List<Row> resultRows = new ArrayList<>(resultRowsMap.values());
        return new QueryResultMsg(resultRows, messages.get(0).getTransaction());
    }

    private QueryResponseMsg getSuccessQuorum(List<QueryResponseMsg> messages) {
        // All queries returned success, so we can return any one of them
        return messages.get(0);
    }

    private QueryResponseMsg getErrorQuorum(List<QueryResponseMsg> messages) {
        // All queries returned an error, so we can return any one of them
        return messages.get(0);
    }
}
