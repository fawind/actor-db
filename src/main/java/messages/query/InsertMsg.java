package messages.query;

import model.LamportQuery;
import model.Row;

public class InsertMsg extends LamportQueryMsg {

    private String tableName;
    private Row row;

    // Used for serialization
    private InsertMsg() {}

    public InsertMsg(String tableName, Row row, LamportQuery lamportQuery) {
        super(lamportQuery);
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
