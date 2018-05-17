package messages.query;

import model.Row;
import model.Transaction;

import java.util.function.Predicate;

public class SelectWhereMsg extends TransactionMsg {

    private String tableName;
    private Predicate<Row> whereFn;

    // Used for serialization
    private SelectWhereMsg() {}

    public SelectWhereMsg(String tableName, Predicate<Row> whereFn, Transaction transaction) {
        super(transaction);
        this.tableName = tableName;
        this.whereFn = whereFn;
    }

    public String getTableName() {
        return tableName;
    }

    public Predicate<Row> getWhereFn() {
        return whereFn;
    }
}
