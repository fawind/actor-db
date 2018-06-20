package store.messages.query;

import api.messages.QueryMetaInfo;

import java.util.List;

public final class CreateTableMsg extends QueryMsg {

    private String tableName;
    private List<String> layout;

    private CreateTableMsg() {}

    public CreateTableMsg(String tableName, List<String> layout, QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
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
