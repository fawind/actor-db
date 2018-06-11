package messages.replication;

import messages.query.LamportQueryMsg;
import model.LamportQuery;

public class ReplicateAckMsg extends LamportQueryMsg {

    // Used for serialization
    private ReplicateAckMsg() {}

    public ReplicateAckMsg(LamportQuery lamportQuery) {
        super(lamportQuery);
    }
}
