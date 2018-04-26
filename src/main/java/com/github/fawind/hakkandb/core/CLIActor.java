package com.github.fawind.hakkandb.core;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class CLIActor extends AbstractLoggingActor {
    public static final String DEFAULT_NAME = "CLIActor";

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Master.QuerySuccessMsg.class, msg -> log.info("Query successful"))
                .match(Master.QueryErrorMsg.class, msg -> log.error("Query error:" + msg.msg))
                .build();
    }

    public static Props props() {
        return Props.create(CLIActor.class);
    }


}
