package messages.query;

import model.Transaction;

public class DropTableMsg extends TransactionMsg {

    private String tableName;

    // Used for serialization
    private DropTableMsg() {}

    public DropTableMsg(String tableName, Transaction transaction) {
        super(transaction);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
