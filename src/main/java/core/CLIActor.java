package core;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.QueryErrorMsg;
import messages.QuerySuccessMsg;

public class CLIActor extends AbstractDBActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(QuerySuccessMsg.class, msg -> log.info("Query successful"))
                .match(QueryErrorMsg.class, msg -> log.error("Query error:" + msg.getMsg()))
                .build();
    }

    public static Props props() {
        return Props.create(CLIActor.class);
    }


}
