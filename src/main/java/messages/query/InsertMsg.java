package messages.query;

import api.messages.QueryMetaInfo;
import model.Row;

public class InsertMsg extends QueryMsg {

    private String tableName;
    private Row row;

    // Used for serialization
    private InsertMsg() {}

    public InsertMsg(String tableName, Row row, QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
        this.tableName = tableName;
        this.row = row;
    }

    public String getTableName() {
        return tableName;
    }

    public Row getRow() {
        return row;
    }
}
