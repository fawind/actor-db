package client.model;

import api.commands.ClientRequest;
import api.commands.Command;
import api.messages.LamportId;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.UUID;

public class ClientRequestFactory {

    private final String clientId;
    private LamportId lamportId;

    @Inject
    public ClientRequestFactory(@Named("ClientId") String clientId) {
        this.clientId = clientId;
        this.lamportId = new LamportId(clientId);
    }

    public ClientRequest buildRequest(Command command) {
        return new ClientRequest(command, getNextRequestId(), incrementedLamportId());
    }

    public void updateLamportId(LamportId responseLamportId) {
        lamportId = lamportId.maxIdCopy(responseLamportId);
    }

    private String getNextRequestId() {
        return UUID.randomUUID().toString();
    }

    private LamportId incrementedLamportId() {
        lamportId = lamportId.incrementedCopy();
        return lamportId;
    }
}
