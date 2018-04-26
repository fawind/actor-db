package messages;

import core.Row;

public class PartitionBlockedMsg {
    private final Row row;

    public PartitionBlockedMsg(Row row) {
        this.row = row;
    }

    public Row getRow() {
        return row;
    }
}
