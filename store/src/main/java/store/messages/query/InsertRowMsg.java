package store.messages.query;

import api.messages.QueryMetaInfo;
import store.model.Row;

public class InsertRowMsg extends QueryMsg {

    private Row row;

    // Used for serialization
    private InsertRowMsg() {}

    public InsertRowMsg(Row row, QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
        this.row = row;
    }

    public Row getRow() {
        return row;
    }
}