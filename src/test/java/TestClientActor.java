import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import core.AbstractClientActor;
import messages.QueryErrorMsg;
import messages.QueryResultMsg;
import messages.QuerySuccessMsg;

public class TestClientActor extends AbstractClientActor {
    protected LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    protected void handleQuerySuccess(QuerySuccessMsg msg) {
        log.info("(TID: {}) Query w/o result successful", msg.getTransactionId());
    }

    @Override
    protected void handleQueryResult(QueryResultMsg msg) {
        log.info("(TID: {}) Query result: {}", msg.getTransactionId(), msg.getResult());
    }

    @Override
    protected void handleQueryError(QueryErrorMsg msg) {
        log.error("(TID: {}) Query error: {}", msg.getTransactionId(), msg.getMsg());
    }

    public static Props props() {
        return Props.create(TestClientActor.class);
    }
}
