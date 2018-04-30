package actors;

import akka.actor.Props;
import messages.query.QueryErrorMsg;
import messages.query.QueryResultMsg;
import messages.query.QuerySuccessMsg;

public class CLIActor extends AbstractDBActor {

    public static final String ACTOR_NAME = "cli";

    public static Props props() {
        return Props.create(CLIActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(QuerySuccessMsg.class, msg -> log.info("Query successful"))
                .match(QueryResultMsg.class, msg -> log.info("Query result\n{}", msg.getResult()))
                .match(QueryErrorMsg.class, msg -> log.error("Query error: {}", msg.getMsg()))
                .matchAny(x -> log.error("Unknown message: {}", x))
                .build();
    }
}
