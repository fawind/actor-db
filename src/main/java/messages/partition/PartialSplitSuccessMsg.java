package messages.partition;

import akka.actor.ActorRef;
import com.google.common.collect.Range;

public class PartialSplitSuccessMsg {

    private final ActorRef newPartition;
    private final Range<Long> newRange;

    public PartialSplitSuccessMsg(ActorRef newPartition, Range<Long> newRange) {
        this.newPartition = newPartition;
        this.newRange = newRange;
    }

    public ActorRef getNewPartition() {
        return newPartition;
    }

    public Range<Long> getNewRange() {
        return newRange;
    }
}
