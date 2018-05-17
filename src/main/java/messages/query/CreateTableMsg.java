package messages.query;

import model.Transaction;

import java.util.List;

public final class CreateTableMsg extends TransactionMsg {

    private String tableName;
    private List<String> layout;

    private CreateTableMsg() {}

    public CreateTableMsg(String tableName, List<String> layout, Transaction transaction) {
        super(transaction);
        this.tableName = tableName;
        this.layout = layout;
    }

    public List<String> getLayout() {
        return layout;
    }

    public String getTableName() {
        return tableName;
    }
}
