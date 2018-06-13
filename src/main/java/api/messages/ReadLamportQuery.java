package api.messages;

import akka.actor.ActorRef;

public class ReadLamportQuery extends LamportQuery {
    public ReadLamportQuery(ActorRef requester, LamportId lamportId) {
        super(requester, lamportId);
    }
}
