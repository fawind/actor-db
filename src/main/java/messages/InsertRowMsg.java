package messages;

import core.Row;

public class InsertRowMsg {
    private final Row row;

    public InsertRowMsg(Row row) {
        this.row = row;
    }

    public Row getRow() {
        return row;
    }
}