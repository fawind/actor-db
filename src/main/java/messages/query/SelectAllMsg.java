package messages.query;

import model.LamportQuery;

public class SelectAllMsg extends LamportQueryMsg {

    private String tableName;

    // Used for serialization
    private SelectAllMsg() {}

    public SelectAllMsg(String tableName, LamportQuery lamportQuery) {
        super(lamportQuery);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
