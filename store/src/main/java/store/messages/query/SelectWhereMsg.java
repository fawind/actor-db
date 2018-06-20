package store.messages.query;

import api.messages.QueryMetaInfo;
import store.model.Row;

import java.util.function.Predicate;

public class SelectWhereMsg extends QueryMsg {

    private String tableName;
    private Predicate<Row> whereFn;

    // Used for serialization
    private SelectWhereMsg() {}

    public SelectWhereMsg(String tableName, Predicate<Row> whereFn, QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
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
