package model;

import akka.actor.ActorRef;

import java.io.Serializable;

public abstract class LamportQuery implements Serializable {
    private LamportId lamportId;
    private ActorRef requester;

    public LamportQuery(ActorRef requester, LamportId lamportId) {
        this.requester = requester;
        this.lamportId = lamportId;
    }

    public LamportId getLamportId() {
        return lamportId;
    }

    public void setLamportId(LamportId lamportId) {
        this.lamportId = lamportId;
    }

    public ActorRef getRequester() {
        return requester;
    }
}
