package core;

import akka.actor.AbstractActor;
import messages.QueryErrorMsg;
import messages.QueryResultMsg;
import messages.QuerySuccessMsg;

public abstract class AbstractClientActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(QuerySuccessMsg.class, this::handleQuerySuccess)
                .match(QueryResultMsg.class, this::handleQueryResult)
                .match(QueryErrorMsg.class, this::handleQueryError)
                .build();
    }

    protected abstract void handleQuerySuccess(QuerySuccessMsg msg);
    protected abstract void handleQueryResult(QueryResultMsg msg);
    protected abstract void handleQueryError(QueryErrorMsg msg);
}
