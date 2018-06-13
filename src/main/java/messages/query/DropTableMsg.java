package messages.query;

import api.messages.LamportQuery;

public class DropTableMsg extends LamportQueryMsg {

    private String tableName;

    // Used for serialization
    private DropTableMsg() {}

    public DropTableMsg(String tableName, LamportQuery lamportQuery) {
        super(lamportQuery);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
