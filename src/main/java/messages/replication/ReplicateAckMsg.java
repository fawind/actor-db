package messages.replication;

import api.messages.LamportQuery;
import messages.query.LamportQueryMsg;

public class ReplicateAckMsg extends LamportQueryMsg {

    // Used for serialization
    private ReplicateAckMsg() {}

    public ReplicateAckMsg(LamportQuery lamportQuery) {
        super(lamportQuery);
    }
}
