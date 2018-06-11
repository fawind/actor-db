package client;

import api.commands.ClientRequest;
import api.commands.Command;
import api.messages.LamportId;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.UUID;

public class ClientRequestFactory {

    private final String clientId;

    @Inject
    public ClientRequestFactory(@Named("ClientId") String clientId) {
        this.clientId = clientId;
    }

    public ClientRequest buildRequest(Command command) {
        // TODO: Implement Lamport
        LamportId lamportId = new LamportId(clientId, getNextRequestId(), -1);
        return new ClientRequest(command, lamportId);
    }

    private String getNextRequestId() {
        return UUID.randomUUID().toString();
    }
}
