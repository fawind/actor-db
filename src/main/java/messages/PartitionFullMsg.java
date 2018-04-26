package messages;

import akka.actor.ActorRef;
import com.google.common.collect.Range;

public class PartitionFullMsg {
    private final ActorRef partition;
    private final Range<Long> oldRange;
    private final Range<Long> newRange;

    public PartitionFullMsg(ActorRef partition, Range<Long> oldRange, Range<Long> newRange) {
        this.partition = partition;
        this.oldRange = oldRange;
        this.newRange = newRange;
    }

    public ActorRef getPartition() {
        return partition;
    }

    public Range<Long> getNewRange() {
        return newRange;
    }
}
