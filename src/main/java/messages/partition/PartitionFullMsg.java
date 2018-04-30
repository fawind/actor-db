package messages.partition;

import com.google.common.collect.Range;

public class PartitionFullMsg {

    private final Range<Long> newRange;

    public PartitionFullMsg(Range<Long> newRange) {
        this.newRange = newRange;
    }

    public Range<Long> getNewRange() {
        return newRange;
    }
}
