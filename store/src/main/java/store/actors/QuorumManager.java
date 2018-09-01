package store.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import api.messages.LamportId;
import api.messages.QueryMetaInfo;
import api.messages.QueryMsg;
import store.configuration.DatastoreConfig;
import store.configuration.DatastoreModule;

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
                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }

    private void handleQuorum(QueryMsg msg) {
        QueryMetaInfo meta = addNewLamportId(msg.getQueryMetaInfo());
        msg.updateMetaInfo(meta);

        int quorumSize = getQuorumSize(msg.getQueryMetaInfo().isWriteQuery());
        ActorRef quorumCollector = getContext().actorOf(QuorumResponseCollector.props(msg.getRequester(), quorumSize));

        memberRegistry.getRandomMasters(quorumSize + config.getEnvConfig().getExtendedQuorum())
                .forEach(actorPath -> getContext().actorSelection(actorPath).tell(msg, quorumCollector));
    }

    private QueryMetaInfo addNewLamportId(QueryMetaInfo queryMetaInfo) {
        return queryMetaInfo.copyWithUpdatedLamportId(updatedLamportId(queryMetaInfo.getLamportId()));
    }

    private LamportId updatedLamportId(LamportId other) {
        lamportId = lamportId.maxIdCopy(other);
        return lamportId;
    }

    private int getQuorumSize(boolean isWriteQuery) {
        if (isWriteQuery) {
            return config.getEnvConfig().getWriteQuorum();
        }
        return config.getEnvConfig().getReadQuorum();
    }
}
