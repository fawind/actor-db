import actors.AbstractClientActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.query.QueryErrorMsg;
import messages.query.QueryResultMsg;
import messages.query.QuerySuccessMsg;

public class TestClientActor extends AbstractClientActor {
    protected LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props() {
        return Props.create(TestClientActor.class);
    }

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
}
