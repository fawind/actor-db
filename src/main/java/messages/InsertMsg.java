package messages;

import core.Row;

public class InsertMsg {
    private final String tableName;
    private final Row row;

    public InsertMsg(String tableName, Row row) {
        this.tableName = tableName;
        this.row = row;
    }

    public String getTableName() {
        return tableName;
    }

    public Row getRow() {
        return row;
    }
}
