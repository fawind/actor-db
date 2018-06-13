package model;

import api.messages.LamportQuery;

import java.io.Serializable;

public class BlockedRow implements Serializable {

    private Row row;
    private LamportQuery lamportQuery;

    // Used for serialization
    private BlockedRow() {}

    public BlockedRow(Row row, LamportQuery lamportQuery) {
        this.row = row;
        this.lamportQuery = lamportQuery;
    }

    public Row getRow() {
        return row;
    }

    public LamportQuery getLamportQuery() {
        return lamportQuery;
    }
}
