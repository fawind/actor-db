package client.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.client.ClusterClient;
import api.AbstractClientActor;
import api.commands.ClientRequest;
import client.ClientRequestFactory;
import client.config.DatastoreClientConfig;
import client.model.CompletableCommand;
import messages.query.QueryResponseMsg;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ClientActor extends AbstractClientActor {

    public static final String ACTOR_NAME = "client-actor";

    public static Props props(
            DatastoreClientConfig config,
            ActorRef clusterClient,
            ClientRequestFactory clientRequestFactory) {
        return Props.create(ClientActor.class, () -> new ClientActor(config, clusterClient, clientRequestFactory));
    }

    private final DatastoreClientConfig config;
    private final ActorRef clusterClient;
    private final ClientRequestFactory clientRequestFactory;

    private final Map<String, CompletableFuture<QueryResponseMsg>> requests = new HashMap<>();

    public ClientActor(
            DatastoreClientConfig config,
            ActorRef clusterClient,
            ClientRequestFactory clientRequestFactory) {
        this.config = config;
        this.clusterClient = clusterClient;
        this.clientRequestFactory = clientRequestFactory;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CompletableCommand.class, this::handleCommand)
                .match(QueryResponseMsg.class, this::handleQueryResponse)
                .matchAny(msg -> log.info("Unknown message: {}", msg))
                .build();
    }

    private void handleCommand(CompletableCommand command) {
        ClientRequest clientRequest = clientRequestFactory.buildRequest(command.getCommand());
        requests.put(clientRequest.getLamportId().getClientRequestId(), command.getResponse());
        clusterClient.tell(new ClusterClient.Send(config.getClientEndpointPath(), clientRequest, true), getSelf());
    }


    protected void handleQueryResponse(QueryResponseMsg msg) {
        String clientRequestId = msg.getLamportId().getClientRequestId();
        if (!requests.containsKey(clientRequestId)) {
            log.error("Client did not send request for response {}", msg);
            return;
        }
        CompletableFuture<QueryResponseMsg> request = requests.get(clientRequestId);
        request.complete(msg);
    }
}
