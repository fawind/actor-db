package messages.query;

import model.Row;
import model.Transaction;

public class InsertRowMsg extends TransactionMsg {

    private Row row;

    // Used for serialization
    private InsertRowMsg() {}

    public InsertRowMsg(Row row, Transaction transaction) {
        super(transaction);
        this.row = row;
    }

    public Row getRow() {
        return row;
    }
}