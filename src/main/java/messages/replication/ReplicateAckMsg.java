package messages.replication;

import messages.query.TransactionMsg;
import model.Transaction;

public class ReplicateAckMsg extends TransactionMsg {

    // Used for serialization
    private ReplicateAckMsg() {}

    public ReplicateAckMsg(Transaction transaction) {
        super(transaction);
    }
}
