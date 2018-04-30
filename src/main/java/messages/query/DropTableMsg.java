package messages.query;

import model.Transaction;

public class DropTableMsg extends TransactionMsg {
    private final String tableName;

    public DropTableMsg(String tableName, Transaction transaction) {
        super(transaction);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
