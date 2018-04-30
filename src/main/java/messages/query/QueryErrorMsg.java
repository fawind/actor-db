package messages.query;

import model.Transaction;

public final class QueryErrorMsg extends TransactionMsg {

    private final String msg;

    public QueryErrorMsg(String msg, Transaction transaction) {
        super(transaction);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
