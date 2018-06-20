package store.messages.query;

import api.messages.QueryMetaInfo;
import api.messages.QueryMsg;

public class SelectAllMsg extends QueryMsg {

    private String tableName;

    // Used for serialization
    private SelectAllMsg() {}

    public SelectAllMsg(String tableName, QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
