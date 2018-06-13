package api;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.query.QueryResponseMsg;

public abstract class AbstractClientActor extends AbstractActor {

    protected LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(QueryResponseMsg.class, this::handleQueryResponse)
                .build();
    }

    protected abstract void handleQueryResponse(QueryResponseMsg msg);
}
