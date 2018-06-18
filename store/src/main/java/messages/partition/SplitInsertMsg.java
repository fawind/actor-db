package messages.partition;

import model.StoredRow;

import java.io.Serializable;
import java.util.List;

public class SplitInsertMsg implements Serializable {

    private List<StoredRow> rows;

    // Used for serialization
    private SplitInsertMsg() {}

    public SplitInsertMsg(List<StoredRow> rows) {
        this.rows = rows;
    }

    public List<StoredRow> getRows() {
        return rows;
    }
}
