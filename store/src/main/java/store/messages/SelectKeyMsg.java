package store.messages;

import api.messages.QueryMetaInfo;
import api.messages.QueryMsg;

public class SelectKeyMsg extends QueryMsg {

    private String tableName;
    private String key;

    // Used for serialization
    private SelectKeyMsg() {}

    public SelectKeyMsg(String tableName, String key, QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
        this.tableName = tableName;
        this.key = key;
    }

    public String getTableName() {
        return tableName;
    }

    public String getKey() {
        return key;
    }
}
