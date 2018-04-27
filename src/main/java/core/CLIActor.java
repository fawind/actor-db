package core;

import akka.actor.Props;
import messages.QueryErrorMsg;
import messages.QueryResultMsg;
import messages.QuerySuccessMsg;

public class CLIActor extends AbstractDBActor {

    public static final String ACTOR_NAME = "cli";

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(QuerySuccessMsg.class, msg -> log.info("Query successful"))
                .match(QueryResultMsg.class, msg -> log.info("Query result\n" + msg.getResult()))
                .match(QueryErrorMsg.class, msg -> log.error("Query error: " + msg.getMsg()))
                .build();
    }

    public static Props props() {
        return Props.create(CLIActor.class);
    }


}
