package messages.query;

import model.Row;
import model.Transaction;

import java.util.List;

public class QueryResultMsg extends TransactionMsg {

    private List<Row> result;

    // Used for serialization
    private QueryResultMsg() {}

    public QueryResultMsg(List<Row> result, Transaction transaction) {
        super(transaction);
        this.result = result;
    }

    public List<Row> getResult() {
        return result;
    }
}
