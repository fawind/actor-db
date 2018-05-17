package messages.query;

import model.Transaction;

public final class QueryErrorMsg extends TransactionMsg {

    private String msg;

    // Used for serialization
    private QueryErrorMsg() {}

    public QueryErrorMsg(String msg, Transaction transaction) {
        super(transaction);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
