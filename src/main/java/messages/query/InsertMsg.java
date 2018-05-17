package messages.query;

import model.Row;
import model.Transaction;

public class InsertMsg extends TransactionMsg {

    private String tableName;
    private Row row;

    // Used for serialization
    private InsertMsg() {}

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
