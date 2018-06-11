package messages.replication;

import messages.query.LamportQueryMsg;
import model.LamportQuery;
import model.Row;

public class ReplicateMsg extends LamportQueryMsg {

    private Row row;

    // Used for serialization
    private ReplicateMsg() {}

    public ReplicateMsg(Row row, LamportQuery lamportQuery) {
        super(lamportQuery);
        this.row = row;
    }

    public Row getRow() {
        return row;
    }
}
