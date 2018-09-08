package store.actors;

import akka.actor.ActorPath;
import akka.actor.ActorPaths;
import akka.cluster.Member;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.String.format;

public class ClusterMemberRegistry {

    private static final Logger log = LoggerFactory.getLogger(ClusterMemberRegistry.class);

    private static final String MASTER_PATH_TEMPLATE = "%s/user/%s";

    private Set<Member> members = new LinkedHashSet<>();

    public void setMembers(Set<Member> newMembers) {
        members = newMembers;
        logMemberStatus();
    }

    public void addMember(Member member) {
        members.add(member);
        logMemberStatus();
    }

    public void removeMember(Member member) {
        members.remove(member);
        logMemberStatus();
    }

    public ImmutableSet<ActorPath> getMasters() {
        return members.stream()
                .map(this::getMasterActorPath)
                .collect(toImmutableSet());
    }

    public ImmutableSet<ActorPath> getRandomMasters(int count) {
        int nodeCount = Math.min(count, members.size());
        List<ActorPath> masters = new ArrayList<>(getMasters());
        Collections.shuffle(masters);
        return ImmutableSet.copyOf(masters.subList(0, nodeCount));
    }

    private ActorPath getMasterActorPath(Member member) {
        String path = format(MASTER_PATH_TEMPLATE, member.address(), Master.ACTOR_NAME);
        return ActorPaths.fromString(path);
    }

    private void logMemberStatus() {
        log.info("Cluster size changed. Members: {}", members.size());
    }
}
