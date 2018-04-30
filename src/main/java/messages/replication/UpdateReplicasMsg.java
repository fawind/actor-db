package messages.replication;

import akka.actor.ActorRef;

import java.util.List;

public class UpdateReplicasMsg {
    private final List<ActorRef> replicas;

    public UpdateReplicasMsg(List<ActorRef> replicas) {
        this.replicas = replicas;
    }

    public List<ActorRef> getReplicas() {
        return replicas;
    }
}
