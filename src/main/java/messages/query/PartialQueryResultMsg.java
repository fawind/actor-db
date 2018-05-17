package messages.query;

import model.Row;
import model.Transaction;

import java.util.List;

public class PartialQueryResultMsg extends TransactionMsg {

    private List<Row> result;
    private int actorId;

    // Used for serialization
    private PartialQueryResultMsg() {}

    public PartialQueryResultMsg(List<Row> result, int actorId, Transaction transaction) {
        super(transaction);
        this.result = result;
        this.actorId = actorId;
    }

    public List<Row> getResult() {
        return result;
    }

    public int getActorId() {
        return actorId;
    }
}
