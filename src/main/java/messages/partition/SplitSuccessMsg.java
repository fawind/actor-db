package messages.partition;

import akka.actor.ActorRef;
import com.google.common.collect.Range;

import java.io.Serializable;

public class SplitSuccessMsg implements Serializable {

    private ActorRef newPartition;
    private Range<Long> newRange;
    private ActorRef oldPartition;
    private Range<Long> oldRange;

    // Used for serialization
    private SplitSuccessMsg() {}

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
