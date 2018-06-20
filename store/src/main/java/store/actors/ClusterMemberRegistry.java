package store.actors;

import akka.actor.ActorPath;
import akka.actor.ActorPaths;
import akka.cluster.Member;
import com.google.common.collect.ImmutableSet;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.String.format;

public class ClusterMemberRegistry {

    private static final String MASTER_PATH_TEMPLATE = "%s/user/%s";

    private Set<Member> members = new LinkedHashSet<>();

    public void setMembers(Set<Member> newMembers) {
        members = newMembers;
    }

    public void addMember(Member member) {
        members.add(member);
    }

    public void removeMember(Member member) {
        members.remove(member);
    }

    public ImmutableSet<ActorPath> getMasters() {
        return members.stream()
                .map(this::getMasterActorPath)
                .collect(toImmutableSet());
    }

    private ActorPath getMasterActorPath(Member member) {
        String path = format(MASTER_PATH_TEMPLATE, member.address(), Master.ACTOR_NAME);
        return ActorPaths.fromString(path);
    }
}
