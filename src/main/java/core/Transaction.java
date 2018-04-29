package core;

import akka.actor.ActorRef;

public class Transaction {
    private final long transactionId;
    private final ActorRef requester;

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
