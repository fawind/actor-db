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
import store.actors.ClientEndpoint;
import store.actors.Master;
import store.actors.QuorumManager;
import store.configuration.DatastoreConfig;

import javax.inject.Inject;
import java.util.UUID;

public class Datastore implements AutoCloseable {

    public static final String SYSTEM_NAME = "actor-db";

    private final DatastoreConfig config;

    private ActorSystem actorSystem;
    private ActorRef quorumManager;
    private ActorRef clientEndpoint;

    @Inject
    public Datastore(DatastoreConfig config) {
        this.config = config;
    }

    public void start() {
        actorSystem = ActorSystem.create(SYSTEM_NAME, config.getAkkaConfig());
        quorumManager = actorSystem.actorOf(QuorumManager.props(), QuorumManager.ACTOR_NAME);
        clientEndpoint = actorSystem.actorOf(ClientEndpoint.props(quorumManager), ClientEndpoint.ACTOR_NAME);
        actorSystem.actorOf(Master.props(), Master.ACTOR_NAME);

        ClusterClientReceptionist.get(actorSystem).registerService(clientEndpoint);

        if (config.getEnvConfig().isBenchmarkTable()) {
            System.out.println("Creating table @ Datastore");
            CreateTableCommand benchmarkTableCmd = CreateTableCommand.builder()
                    .tableName("usertable")
                    .schema(ImmutableList.of("string", "string"))
                    .build();

            ClientRequest benchmarkTableRequest = new ClientRequest(benchmarkTableCmd, UUID.randomUUID().toString(), new LamportId("Datastore"));
            clientEndpoint.tell(benchmarkTableRequest, ActorRef.noSender());
        }
    }

    @Override
    public void close() {
        actorSystem.terminate();
    }
}
