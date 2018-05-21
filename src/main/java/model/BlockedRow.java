package model;

import java.io.Serializable;

public class BlockedRow implements Serializable {

    private Row row;
    private Transaction transaction;

    // Used for serialization
    private BlockedRow() {}

    public BlockedRow(Row row, Transaction transaction) {
        this.row = row;
        this.transaction = transaction;
    }

    public Row getRow() {
        return row;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
