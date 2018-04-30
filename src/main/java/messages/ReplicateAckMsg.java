package messages;

import messages.query.TransactionMsg;
import model.Transaction;

public class ReplicateAckMsg extends TransactionMsg {
    public ReplicateAckMsg(Transaction transaction) {
        super(transaction);
    }
}
