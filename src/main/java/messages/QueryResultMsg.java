package messages;

import core.Row;

import java.util.List;

public class QueryResultMsg extends TransactionMsg {
    private final List<Row> result;

    public QueryResultMsg(List<Row> result, TransactionMsg transactionMsg) {
        super(transactionMsg.transactionId, transactionMsg.requester);
        this.result = result;
    }

    public List<Row> getResult() {
        return result;
    }
}
