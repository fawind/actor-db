package actors;

import akka.actor.AbstractActor;
import messages.query.QueryErrorMsg;
import messages.query.QueryResultMsg;
import messages.query.QuerySuccessMsg;

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
