package store.messages.query;

import api.messages.QueryMetaInfo;
import api.messages.QueryMsg;
import api.model.Row;

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