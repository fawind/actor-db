package api.messages;

import akka.actor.ActorRef;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class LamportQuery {

    // Used for serialization
    private LamportQuery() {}

    private ActorRef requester;
    private LamportId lamportId;
}
