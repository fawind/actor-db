package model;

import akka.actor.ActorRef;
import lombok.Data;

import java.io.Serializable;

public class Transaction implements Serializable {

    private long transactionId;
    private ActorRef requester;

    // Used for serialization
    private Transaction() {}

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
