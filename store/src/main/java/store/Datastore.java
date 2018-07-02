package store;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.client.ClusterClientReceptionist;
import kamon.Kamon;
import kamon.prometheus.PrometheusReporter;
import kamon.zipkin.ZipkinReporter;
import store.actors.ClientEndpoint;
import store.actors.Master;
import store.actors.MetricsListener;
import store.actors.QuorumManager;
import store.configuration.DatastoreConfig;

import javax.inject.Inject;

public class Datastore implements AutoCloseable {

    public static final String SYSTEM_NAME = "actor-db";

    private final DatastoreConfig config;

    private ActorSystem actorSystem;
    private ActorRef quorumManager;
    private ActorRef clientEndpoint;
    private ActorRef metricsListener;

    @Inject
    public Datastore(DatastoreConfig config) {
        this.config = config;
    }

    public void start() {
        Kamon.addReporter(new PrometheusReporter());
        Kamon.addReporter(new ZipkinReporter());

        actorSystem = ActorSystem.create(SYSTEM_NAME, config.getAkkaConfig());
        quorumManager = actorSystem.actorOf(QuorumManager.props(), QuorumManager.ACTOR_NAME);
        clientEndpoint = actorSystem.actorOf(ClientEndpoint.props(quorumManager), ClientEndpoint.ACTOR_NAME);
        actorSystem.actorOf(Master.props(), Master.ACTOR_NAME);
//        metricsListener = actorSystem.actorOf(MetricsListener.props(), MetricsListener.ACTOR_NAME);

        ClusterClientReceptionist.get(actorSystem).registerService(clientEndpoint);
    }

    @Override
    public void close() {
        actorSystem.terminate();
    }
}
