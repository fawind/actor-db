package model;

import akka.actor.ActorRef;

import java.io.Serializable;

public abstract class Transaction implements Serializable {

    private long transactionId;
    private ActorRef requester;

    public Transaction(long transactionId, ActorRef requester) {
        this.transactionId = transactionId;
        this.requester = requester;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public ActorRef getRequester() {
        return requester;
    }
}
