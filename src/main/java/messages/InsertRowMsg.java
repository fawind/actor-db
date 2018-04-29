package messages;

import core.Row;
import core.Transaction;

public class InsertRowMsg extends TransactionMsg {
    private final Row row;

    public InsertRowMsg(Row row, Transaction transaction) {
        super(transaction);
        this.row = row;
    }

    public Row getRow() {
        return row;
    }
}