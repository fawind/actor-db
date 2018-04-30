package messages.partition;

import model.BlockedRow;

public class PartitionBlockedMsg {

    private final BlockedRow blockedRow;

    public PartitionBlockedMsg(BlockedRow blockedRow) {
        this.blockedRow = blockedRow;
    }

    public BlockedRow getBlockedRow() {
        return blockedRow;
    }
}
