package model;

import akka.actor.ActorRef;

public class WriteLamportQuery extends LamportQuery {
    public WriteLamportQuery(ActorRef requester, LamportId lamportId) {
        super(requester, lamportId);
    }
}
