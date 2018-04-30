package messages.query;

import akka.actor.ActorRef;
import model.Transaction;

public abstract class TransactionMsg {

    protected final Transaction transaction;

    public TransactionMsg(Transaction transaction) {
        this.transaction = transaction;
    }

    public final Transaction getTransaction() {
        return transaction;
    }

    public final long getTransactionId() {
        return transaction.getTransactionId();
    }

    public final ActorRef getRequester() {
        return transaction.getRequester();
    }
}
