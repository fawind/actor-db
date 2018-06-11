package messages.query;

import model.LamportQuery;
import model.Row;

import java.util.function.Predicate;

public class SelectWhereMsg extends LamportQueryMsg {

    private String tableName;
    private Predicate<Row> whereFn;

    // Used for serialization
    private SelectWhereMsg() {}

    public SelectWhereMsg(String tableName, Predicate<Row> whereFn, LamportQuery lamportQuery) {
        super(lamportQuery);
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
