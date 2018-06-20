package store.messages.replication;

import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.List;

public class UpdateReplicasMsg implements Serializable {

    private List<ActorRef> replicas;

    // Used for serialization
    private UpdateReplicasMsg() {}

    public UpdateReplicasMsg(List<ActorRef> replicas) {
        this.replicas = replicas;
    }

    public List<ActorRef> getReplicas() {
        return replicas;
    }
}
