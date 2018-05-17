package messages.partition;

import model.BlockedRow;

import java.io.Serializable;

public class PartitionBlockedMsg implements Serializable {

    private BlockedRow blockedRow;

    // Used for serialization
    private PartitionBlockedMsg() {}

    public PartitionBlockedMsg(BlockedRow blockedRow) {
        this.blockedRow = blockedRow;
    }

    public BlockedRow getBlockedRow() {
        return blockedRow;
    }
}
