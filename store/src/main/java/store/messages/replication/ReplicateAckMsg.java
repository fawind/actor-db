package store.messages.replication;

import api.messages.QueryMetaInfo;
import store.messages.query.QueryMsg;

public class ReplicateAckMsg extends QueryMsg {

    // Used for serialization
    private ReplicateAckMsg() {}

    public ReplicateAckMsg(QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
    }
}
