package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import messages.query.QueryResponseMsg;
import messages.query.TransactionMsg;
import model.WriteTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class deals with all quorum-related matters. It is aware of all master nodes in the network.
 */
public class QuorumManager extends AbstractDBActor {

    public static final String ACTOR_NAME = "quorum-manager";
    private static final int READ_QUORUM = 2;
    private static final int WRITE_QUORUM = 2;

    public static Props props() {
        return Props.create(QuorumManager.class, QuorumManager::new);
    }

    private final ClusterMemberRegistry memberRegistry;
    private final Map<Long, List<QueryResponseMsg>> quorumResponses;

    private QuorumManager() {
        memberRegistry = new ClusterMemberRegistry();
        quorumResponses = new HashMap<>();
        getContext().actorOf(ClusterMemberListener.props(memberRegistry), ClusterMemberListener.ACTOR_NAME);
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
        memberRegistry.getMasters()
                .forEach(actorPath -> getContext().actorFor(actorPath).tell(msg, getSelf()));
    }

    private QueryResponseMsg getQuorumResponse(List<QueryResponseMsg> messages) {
        // TODO: compare messages to get correct result
        log.info("Quorum for TID {}", messages.get(0).getTransactionId());
        return messages.get(0);
    }
}
