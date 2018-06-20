package store.model;

import api.messages.QueryMetaInfo;
import api.model.Row;

import java.io.Serializable;

public class BlockedRow implements Serializable {

    private Row row;
    private QueryMetaInfo queryMetaInfo;

    // Used for serialization
    private BlockedRow() {}

    public BlockedRow(Row row, QueryMetaInfo queryMetaInfo) {
        this.row = row;
        this.queryMetaInfo = queryMetaInfo;
    }

    public Row getRow() {
        return row;
    }

    public QueryMetaInfo getQueryMetaInfo() {
        return queryMetaInfo;
    }
}
