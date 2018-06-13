package messages.query;

import api.messages.LamportQuery;
import model.Row;

public class InsertRowMsg extends LamportQueryMsg {

    private Row row;

    // Used for serialization
    private InsertRowMsg() {}

    public InsertRowMsg(Row row, LamportQuery lamportQuery) {
        super(lamportQuery);
        this.row = row;
    }

    public Row getRow() {
        return row;
    }
}