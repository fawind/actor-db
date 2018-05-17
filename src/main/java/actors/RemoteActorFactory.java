package actors;

import akka.cluster.Member;

import java.util.LinkedHashSet;
import java.util.Set;

public class RemoteActorFactory {

    private Set<Member> members = new LinkedHashSet<>();

    public void setMembers(Set<Member> newMembers) {
        this.members = newMembers;
    }

    public void addMember(Member member) {
        members.add(member);
    }

    public void removeMember(Member member) {
        members.remove(member);
    }

    public Member getRandomMember() {
        // TODO: Improve using random access collection
        return members.stream()
                .skip((int) (members.size() * Math.random()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No nodes registered"));
    }
}
