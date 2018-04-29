package messages;

import akka.actor.ActorRef;

public class SplitPartitionMsg {

    private final ActorRef newPartition;

    public SplitPartitionMsg(ActorRef newPartition) {
        this.newPartition = newPartition;
    }

    public ActorRef getNewPartition() {
        return newPartition;
    }
}
