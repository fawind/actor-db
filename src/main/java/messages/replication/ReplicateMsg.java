package messages.replication;

import messages.query.TransactionMsg;
import model.Row;
import model.Transaction;

public class ReplicateMsg extends TransactionMsg {
    private final Row row;

    public ReplicateMsg(Row row, Transaction transaction) {
        super(transaction);
        this.row = row;
    }

    public Row getRow() {
        return row;
    }
}
