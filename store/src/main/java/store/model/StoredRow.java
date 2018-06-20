package store.model;

import api.messages.LamportId;

import java.io.Serializable;

public class StoredRow implements Serializable {
    private final Row row;
    private final LamportId lamportId;

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
