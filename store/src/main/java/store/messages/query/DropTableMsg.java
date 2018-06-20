package store.messages.query;

import api.messages.QueryMetaInfo;

public class DropTableMsg extends QueryMsg {

    private String tableName;

    // Used for serialization
    private DropTableMsg() {}

    public DropTableMsg(String tableName, QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
