import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import api.AbstractClientActor;
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
        log.info("({}) QueryMetaInfo w/o result successful", msg.getLamportId());
    }

    @Override
    protected void handleQueryResult(QueryResultMsg msg) {
        log.info("({}) QueryMetaInfo result: {}", msg.getLamportId(), msg.getResult());
    }

    @Override
    protected void handleQueryError(QueryErrorMsg msg) {
        log.error("({}) QueryMetaInfo error: {}", msg.getLamportId(), msg.getMsg());
    }
}
