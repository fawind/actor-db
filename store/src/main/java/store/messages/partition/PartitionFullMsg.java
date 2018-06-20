package store.messages.partition;

import com.google.common.collect.Range;

import java.io.Serializable;

public class PartitionFullMsg implements Serializable {

    private Range<Long> newRange;

    // Used for serialization
    private PartitionFullMsg() {}

    public PartitionFullMsg(Range<Long> newRange) {
        this.newRange = newRange;
    }

    public Range<Long> getNewRange() {
        return newRange;
    }
}
