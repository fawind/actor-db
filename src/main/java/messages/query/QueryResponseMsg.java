package messages.query;

import model.Transaction;

public class QueryResponseMsg extends TransactionMsg {
    protected QueryResponseMsg() {}

    public QueryResponseMsg(Transaction transaction) {
        super(transaction);
    }
}
