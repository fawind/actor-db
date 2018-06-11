package client.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.client.ClusterClient;
import api.AbstractClientActor;
import api.commands.ClientRequest;
import client.ClientRequestFactory;
import client.config.DatastoreClientConfig;
import client.model.CompletableCommand;
import messages.query.QueryErrorMsg;
import messages.query.QueryResponseMsg;
import messages.query.QueryResultMsg;
import messages.query.QuerySuccessMsg;

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
                .match(QuerySuccessMsg.class, this::handleQuerySuccess)
                .match(QueryResultMsg.class, this::handleQueryResult)
                .match(QueryErrorMsg.class, this::handleQueryError)
                .build();
    }

    private void handleCommand(CompletableCommand command) {
        ClientRequest clientRequest = clientRequestFactory.buildRequest(command.getCommand());
        requests.put(clientRequest.getLamportId().getClientRequestId(), command.getResponse());
        clusterClient.tell(new ClusterClient.Send(config.getClientEndpointPath(), clientRequest, true), getSelf());
        log.info("Send out msg");
    }


    @Override
    protected void handleQuerySuccess(QuerySuccessMsg msg) {
        log.info("QuerySuccess: " + msg);
    }

    @Override
    protected void handleQueryResult(QueryResultMsg msg) {
        log.info("QueryResult: " + msg);

    }

    @Override
    protected void handleQueryError(QueryErrorMsg msg) {
        log.info("QueryError: " + msg);
    }
}
