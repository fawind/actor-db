package messages;

import akka.actor.ActorRef;

public abstract class TransactionMsg {
    protected final long transactionId;
    protected final ActorRef requester;

    public TransactionMsg(long transactionId, ActorRef requester) {
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
