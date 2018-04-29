package model;

import lombok.Data;

@Data
public class BlockedRow {

    private final Row row;
    private final Transaction transaction;

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
