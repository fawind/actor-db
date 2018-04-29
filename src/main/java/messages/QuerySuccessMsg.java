package messages;

import core.Transaction;

public final class QuerySuccessMsg extends TransactionMsg {
    public QuerySuccessMsg(Transaction transaction) {
        super(transaction);
    }
}
