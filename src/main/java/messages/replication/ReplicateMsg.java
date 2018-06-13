package messages.replication;

import api.messages.QueryMetaInfo;
import messages.query.QueryMsg;
import model.Row;

public class ReplicateMsg extends QueryMsg {

    private Row row;

    // Used for serialization
    private ReplicateMsg() {}

    public ReplicateMsg(Row row, QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
        this.row = row;
    }

    public Row getRow() {
        return row;
    }
}
