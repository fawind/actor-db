package messages;

import core.Transaction;

public final class CreateTableMsg extends TransactionMsg {
    private final String tableName;
    private final String layout;

    public CreateTableMsg(String tableName, String layout, Transaction transaction) {
        super(transaction);
        this.tableName = tableName;
        this.layout = layout;
    }

    public String getLayout() {
        return layout;
    }

    public String getTableName() {
        return tableName;
    }
}
