package messages;

import core.Row;
import core.Transaction;

public class InsertMsg extends TransactionMsg {
    private final String tableName;
    private final Row row;

    public InsertMsg(String tableName, Row row, Transaction transaction) {
        super(transaction);
        this.tableName = tableName;
        this.row = row;
    }

    public String getTableName() {
        return tableName;
    }

    public Row getRow() {
        return row;
    }
}
