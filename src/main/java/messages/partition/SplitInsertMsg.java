package messages.partition;

import model.Row;

import java.io.Serializable;
import java.util.List;

public class SplitInsertMsg implements Serializable {

    private List<Row> rows;

    // Used for serialization
    private SplitInsertMsg() {}

    public SplitInsertMsg(List<Row> rows) {
        this.rows = rows;
    }

    public List<Row> getRows() {
        return rows;
    }
}
