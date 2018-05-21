package actors;

import akka.actor.AbstractLoggingActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.google.common.collect.Streams;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractDBActor extends AbstractLoggingActor {

    private final RemoteActorFactory remoteActorFactory = new RemoteActorFactory();

    protected LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    protected Cluster cluster = Cluster.get(getContext().getSystem());

    @Override
    public void preStart() throws Exception {
        super.preStart();
//        cluster.subscribe(getSelf(), ClusterEvent.MemberEvent.class);
    }

    protected ReceiveBuilder getClusterAwareReceiveBuilder() {
        return receiveBuilder()
                .match(ClusterEvent.CurrentClusterState.class, this::handleCurrentClusterState)
                .match(ClusterEvent.MemberUp.class, this::handleMemberUp)
                .match(ClusterEvent.MemberRemoved.class, this::handleMemberRemoved);
    }

    private void handleCurrentClusterState(ClusterEvent.CurrentClusterState msg) {
        Set<Member> activeMembers = Streams.stream(msg.getMembers())
                .filter(member -> member.status().equals(MemberStatus.up()))
                .collect(Collectors.toSet());
        remoteActorFactory.setMembers(activeMembers);
    }

    private void handleMemberUp(ClusterEvent.MemberUp msg) {
        remoteActorFactory.addMember(msg.member());
    }

    private void handleMemberRemoved(ClusterEvent.MemberRemoved msg) {
        remoteActorFactory.removeMember(msg.member());
    }
}
