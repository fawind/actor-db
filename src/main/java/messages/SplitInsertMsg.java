package messages;

import core.Row;

import java.util.List;

public class SplitInsertMsg {
    private final List<Row> rows;

    public SplitInsertMsg(List<Row> rows) {
        this.rows = rows;
    }

    public List<Row> getRows() {
        return rows;
    }
}
