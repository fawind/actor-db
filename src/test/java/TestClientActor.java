import api.AbstractClientActor;
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
        log.info("({}) Query w/o result successful", msg.getLamportId());
    }

    @Override
    protected void handleQueryResult(QueryResultMsg msg) {
        log.info("({}) Query result: {}", msg.getLamportId(), msg.getResult());
    }

    @Override
    protected void handleQueryError(QueryErrorMsg msg) {
        log.error("({}) Query error: {}", msg.getLamportId(), msg.getMsg());
    }
}
