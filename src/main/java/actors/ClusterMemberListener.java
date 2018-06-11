package actors;

import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import com.google.common.collect.Streams;

import java.util.Set;
import java.util.stream.Collectors;

public class ClusterMemberListener extends AbstractDBActor {

    public static final String ACTOR_NAME = "cluster-member-listener";
    private final ClusterMemberRegistry memberRegistry;
    private Cluster cluster = Cluster.get(getContext().getSystem());
    public ClusterMemberListener(ClusterMemberRegistry memberRegistry) {
        this.memberRegistry = memberRegistry;
    }

    public static Props props(ClusterMemberRegistry memberRegistry) {
        return Props.create(ClusterMemberListener.class, () -> new ClusterMemberListener(memberRegistry));
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        cluster.subscribe(getSelf(), ClusterEvent.MemberEvent.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ClusterEvent.CurrentClusterState.class, this::handleCurrentClusterState)
                .match(ClusterEvent.MemberUp.class, this::handleMemberUp)
                .match(ClusterEvent.MemberRemoved.class, this::handleMemberRemoved)
                .build();
    }

    private void handleCurrentClusterState(ClusterEvent.CurrentClusterState msg) {
        Set<Member> activeMembers = Streams.stream(msg.getMembers())
                .filter(member -> member.status().equals(MemberStatus.up()))
                .collect(Collectors.toSet());
        memberRegistry.setMembers(activeMembers);
    }

    private void handleMemberUp(ClusterEvent.MemberUp msg) {
        memberRegistry.addMember(msg.member());
    }

    private void handleMemberRemoved(ClusterEvent.MemberRemoved msg) {
        memberRegistry.removeMember(msg.member());
    }
}
