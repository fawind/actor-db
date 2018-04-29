package messages;

import core.Row;
import core.Transaction;

import java.util.List;

public class QueryResultMsg extends TransactionMsg {
    private final List<Row> result;

    public QueryResultMsg(List<Row> result, Transaction transaction) {
        super(transaction);
        this.result = result;
    }

    public List<Row> getResult() {
        return result;
    }
}
