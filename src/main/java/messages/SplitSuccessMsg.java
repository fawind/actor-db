package messages;

import akka.actor.ActorRef;
import com.google.common.collect.Range;

public class SplitSuccessMsg {

    private final ActorRef newPartition;
    private final Range<Long> newRange;
    private final ActorRef oldPartition;
    private final Range<Long> oldRange;

    public SplitSuccessMsg(ActorRef newPartition, Range<Long> newRange, ActorRef oldPartition, Range<Long> oldRange) {
        this.newPartition = newPartition;
        this.newRange = newRange;
        this.oldPartition = oldPartition;
        this.oldRange = oldRange;
    }

    public ActorRef getNewPartition() {
        return newPartition;
    }

    public Range<Long> getNewRange() {
        return newRange;
    }

    public ActorRef getOldPartition() {
        return oldPartition;
    }

    public Range<Long> getOldRange() {
        return oldRange;
    }
}
