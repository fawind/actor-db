package store.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import api.messages.LamportId;
import api.messages.QueryMetaInfo;
import store.messages.query.PartialQueryResultMsg;
import api.messages.QueryErrorMsg;
import api.messages.QueryMsg;
import api.messages.QueryResponseMsg;
import api.messages.QueryResultMsg;
import api.messages.QuerySuccessMsg;
import api.model.Row;
import store.model.StoredRow;

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
    private LamportId lamportId = new LamportId(id);

    private QuorumManager() {
        memberRegistry = new ClusterMemberRegistry();
        getContext().actorOf(ClusterMemberListener.props(memberRegistry), ClusterMemberListener.ACTOR_NAME);
    }

    public static Props props() {
        return Props.create(QuorumManager.class, QuorumManager::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(QueryMsg.class, this::handleQuorum)
                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }


    private void handleQuorum(QueryMsg msg) {
        QueryMetaInfo meta = addNewLamportId(msg.getQueryMetaInfo());
        msg.updateMetaInfo(meta);

        int quorumSize = msg.getQueryMetaInfo().isWriteQuery() ? WRITE_QUORUM : READ_QUORUM;
        ActorRef quorumCollector = getContext().actorOf(QuorumResponseCollector.props(msg.getRequester(), quorumSize));

        memberRegistry.getMasters()
                .forEach(actorPath -> getContext().actorSelection(actorPath).tell(msg, quorumCollector));
    }

    private QueryMetaInfo addNewLamportId(QueryMetaInfo queryMetaInfo) {
        return queryMetaInfo.copyWithUpdatedLamportId(updatedLamportId(queryMetaInfo.getLamportId()));
    }

    private LamportId updatedLamportId(LamportId other) {
        lamportId = lamportId.maxIdCopy(other);
        return lamportId;
    }
}
