package messages.query;

import model.Transaction;

public final class SelectAllMsg extends TransactionMsg {

    private final String tableName;

    public SelectAllMsg(String tableName, Transaction transaction) {
        super(transaction);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
