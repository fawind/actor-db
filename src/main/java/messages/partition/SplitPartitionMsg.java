package messages.partition;

import akka.actor.ActorRef;

import java.io.Serializable;

public class SplitPartitionMsg implements Serializable {

    private ActorRef newPartition;

    // Used for serialization
    private SplitPartitionMsg() {}

    public SplitPartitionMsg(ActorRef newPartition) {
        this.newPartition = newPartition;
    }

    public ActorRef getNewPartition() {
        return newPartition;
    }
}
