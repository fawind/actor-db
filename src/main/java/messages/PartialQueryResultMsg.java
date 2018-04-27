package messages;

import core.Row;

import java.util.List;

public class PartialQueryResultMsg extends TransactionMsg {
    private final List<Row> result;
    private final int actorId;

    public PartialQueryResultMsg(List<Row> result, int actorId, TransactionMsg transactionMsg) {
        super(transactionMsg.transactionId, transactionMsg.requester);
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
