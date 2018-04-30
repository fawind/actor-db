package messages.query;

import model.Transaction;

public final class QuerySuccessMsg extends TransactionMsg {
    public QuerySuccessMsg(Transaction transaction) {
        super(transaction);
    }
}
