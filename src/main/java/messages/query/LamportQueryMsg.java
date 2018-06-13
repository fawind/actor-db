package messages.query;

import akka.actor.ActorRef;
import api.messages.LamportId;
import api.messages.LamportQuery;

import java.io.Serializable;

public abstract class LamportQueryMsg implements Serializable {

    protected LamportQuery lamportQuery;

    // Used for serialization
    protected LamportQueryMsg() {}

    public LamportQueryMsg(LamportQuery lamportQuery) {
        this.lamportQuery = lamportQuery;
    }

    public LamportQuery getLamportQuery() {
        return lamportQuery;
    }

    public LamportId getLamportId() {
        return lamportQuery.getLamportId();
    }

    public ActorRef getRequester() {
        return lamportQuery.getRequester();
    }
}
