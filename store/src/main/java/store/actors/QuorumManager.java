package store.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import api.messages.LamportId;
import api.messages.QueryErrorMsg;
import api.messages.QueryMetaInfo;
import api.messages.QueryMsg;
import api.messages.QueryResponseMsg;
import api.messages.QueryResultMsg;
import api.model.Row;
import store.configuration.DatastoreConfig;
import store.configuration.DatastoreModule;
import store.messages.query.PartialQueryResultMsg;
import store.model.StoredRow;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class deals with all quorum-related matters. It is aware of all master nodes in the network.
 */
public class QuorumManager extends AbstractDBActor {

    public static final String ACTOR_NAME = "quorum-manager";

    private final String id = UUID.randomUUID().toString();
    private final DatastoreConfig config;
    private final ClusterMemberRegistry memberRegistry;
    private LamportId lamportId = new LamportId(id);

    private QuorumManager() {
        config = DatastoreModule.inject(DatastoreConfig.class);
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
                .match(QueryResponseMsg.class, this::handleQueryResponseMsg)
                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }

    private void handleQuorum(QueryMsg msg) {
        QueryMetaInfo meta = addNewLamportId(msg.getQueryMetaInfo());
        msg.updateMetaInfo(meta);

        int quorumSize = msg.getQueryMetaInfo().isWriteQuery() ? config.getEnvConfig().getWriteQuorum() : config.getEnvConfig().getReadQuorum();
        ActorRef quorumCollector = getContext().actorOf(QuorumResponseCollector.props(msg.getRequester(), quorumSize));

        memberRegistry.getMasters()
//                .forEach(actorPath -> getContext().actorSelection(actorPath).tell(msg, quorumCollector));
                .forEach(actorPath -> getContext().actorSelection(actorPath).tell(msg, getSelf()));
    }

    private QueryMetaInfo addNewLamportId(QueryMetaInfo queryMetaInfo) {
        return queryMetaInfo.copyWithUpdatedLamportId(updatedLamportId(queryMetaInfo.getLamportId()));
    }

    private LamportId updatedLamportId(LamportId other) {
        lamportId = lamportId.maxIdCopy(other);
        return lamportId;
    }

    private void handleQueryResponseMsg(QueryResponseMsg msg) {
        lamportId = lamportId.max(msg.getLamportId());

        // For now, if we see an error we assume everything is an error
        if (msg instanceof QueryErrorMsg) {
            tellWithNewestResponseLamportId(msg);
            return;
        }

        // Seen all results, pass result to quorumManager
//        QueryResponseMsg quorumResponse = getQuorumResponse();
        PartialQueryResultMsg resultMsg = (PartialQueryResultMsg) msg;
        List<StoredRow> storedRows = resultMsg.getResult();
        List<Row> results = new ArrayList<>();
        for (StoredRow sr : storedRows) {
            results.add(sr.getRow());
        }

        QueryResponseMsg quorumResponse = new QueryResultMsg(results, msg.getQueryMetaInfo());
        tellWithNewestResponseLamportId(quorumResponse);
    }

    private void tellWithNewestResponseLamportId(QueryMsg msg) {
        msg.updateMetaInfo(addResponseLamportIdToMeta(msg.getQueryMetaInfo()));
        msg.getQueryMetaInfo().getRequester().tell(msg, ActorRef.noSender());
    }

    private QueryMetaInfo addResponseLamportIdToMeta(QueryMetaInfo metaInfo) {
        return metaInfo.copyWithResponseLamportId(updatedLamportId(metaInfo.getLamportId()));
    }
}
