package store.messages.query;

import api.messages.QueryMetaInfo;
import api.messages.QueryMsg;

public class DeleteKeyMsg extends QueryMsg {

    private String tableName;
    private String key;

    // Used for serialization
    private DeleteKeyMsg() {}

    public DeleteKeyMsg(String tableName, String key, QueryMetaInfo queryMetaInfo) {
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
