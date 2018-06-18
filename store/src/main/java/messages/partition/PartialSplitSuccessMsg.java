package messages.partition;

import akka.actor.ActorRef;
import com.google.common.collect.Range;

import java.io.Serializable;

public class PartialSplitSuccessMsg implements Serializable {

    private ActorRef newPartition;
    private Range<Long> newRange;

    // Used for serialization
    private PartialSplitSuccessMsg() {}

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
