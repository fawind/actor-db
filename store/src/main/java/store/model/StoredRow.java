package store.model;

import api.messages.LamportId;
import api.model.Row;

import java.io.Serializable;

public class StoredRow implements Serializable {
    private Row row;
    private LamportId lamportId;

    private StoredRow() {}

    public StoredRow(Row row, LamportId lamportId) {
        this.row = row;
        this.lamportId = lamportId;
    }

    public Row getRow() {
        return row;
    }

    public LamportId getLamportId() {
        return lamportId;
    }
}
