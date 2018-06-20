package client;

import client.actors.ClientActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.client.ClusterClient;
import akka.cluster.client.ClusterClientSettings;
import api.commands.Command;
import client.model.CompletableCommand;
import client.config.DatastoreClientConfig;
import store.messages.query.QueryResponseMsg;
import client.model.ClientRequestFactory;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class DatastoreClient implements AutoCloseable {

    public static final String SYSTEM_NAME = "actors-db-client";
    public static final String CLUSTER_CLIENT_NAME = "cluster-client";

    private final DatastoreClientConfig config;
    private final ClientRequestFactory clientRequestFactory;
    private ActorSystem actorSystem;
    private ActorRef clusterClient;
    private ActorRef clientActor;

    @Inject
    public DatastoreClient(DatastoreClientConfig config, ClientRequestFactory clientRequestFactory) {
        this.config = config;
        this.clientRequestFactory = clientRequestFactory;
    }

    public void start() {
        actorSystem = ActorSystem.create(SYSTEM_NAME, config.getAkkaConfig());
        clusterClient = actorSystem.actorOf(ClusterClient.props(getClusterClientSettings()), CLUSTER_CLIENT_NAME);
        clientActor = actorSystem.actorOf(ClientActor.props(config, clusterClient, clientRequestFactory), ClientActor
                .ACTOR_NAME);
    }

    public CompletableFuture<QueryResponseMsg> sendRequest(Command command) {
        CompletableCommand completableCommand = CompletableCommand.fromCommand(command);
        clientActor.tell(completableCommand, ActorRef.noSender());
        return completableCommand.getResponse();
    }

    @Override
    public void close() {
        actorSystem.terminate();
    }

    private ClusterClientSettings getClusterClientSettings() {
        return ClusterClientSettings.create(actorSystem).withInitialContacts(config.getInitialContacts());
    }
}
