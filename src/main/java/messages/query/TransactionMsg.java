package messages.query;

import akka.actor.ActorRef;
import model.Transaction;

import java.io.Serializable;

public abstract class TransactionMsg implements Serializable {

    protected Transaction transaction;

    // Used for serialization
    protected TransactionMsg() {}

    public TransactionMsg(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public long getTransactionId() {
        return transaction.getTransactionId();
    }

    public ActorRef getRequester() {
        return transaction.getRequester();
    }
}
