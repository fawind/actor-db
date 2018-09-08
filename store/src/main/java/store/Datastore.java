package store;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.client.ClusterClientReceptionist;
import api.commands.CreateTableCommand;
import api.messages.ClientRequest;
import api.messages.LamportId;
import com.google.common.collect.ImmutableList;
import kamon.Kamon;
import kamon.prometheus.PrometheusReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import store.actors.ClientEndpoint;
import store.actors.Master;
import store.actors.QuorumManager;
import store.configuration.DatastoreConfig;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.UUID;

public class Datastore implements AutoCloseable {

    public static final String SYSTEM_NAME = "actor-db";
    private static final Logger log = LoggerFactory.getLogger(Datastore.class);

    private final DatastoreConfig config;

    private ActorSystem actorSystem;
    private ActorRef quorumManager;
    private ActorRef clientEndpoint;

    @Inject
    public Datastore(DatastoreConfig config) {
        this.config = config;
    }

    public void start() {
        log.info("Starting datastore with config: {}", config.getEnvConfig());
        actorSystem = ActorSystem.create(SYSTEM_NAME, config.getAkkaConfig());
        quorumManager = actorSystem.actorOf(QuorumManager.props(), QuorumManager.ACTOR_NAME);
        clientEndpoint = actorSystem.actorOf(ClientEndpoint.props(quorumManager), ClientEndpoint.ACTOR_NAME);
        actorSystem.actorOf(Master.props(), Master.ACTOR_NAME);

        ClusterClientReceptionist.get(actorSystem).registerService(clientEndpoint);
    }

    @Override
    public void close() {
        actorSystem.terminate();
    }
}
