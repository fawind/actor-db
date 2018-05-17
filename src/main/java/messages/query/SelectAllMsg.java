package messages.query;

import model.Transaction;

public class SelectAllMsg extends TransactionMsg {

    private String tableName;

    // Used for serialization
    private SelectAllMsg() {}

    public SelectAllMsg(String tableName, Transaction transaction) {
        super(transaction);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
