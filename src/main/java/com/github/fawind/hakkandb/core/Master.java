package com.github.fawind.hakkandb.core;

import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.HashMap;
import java.util.Map;

public class Master extends AbstractActor {

    private Map<String, ActorRef> tables;

    private Master() {
        this.tables = new HashMap<>();
    }

    static Props props() {
        return Props.create(Master.class, Master::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateTableMsg.class, this::handleCreateTableMsg)
                .match(SelectAllMsg.class, this::handleSelectAllMsg)
                .build();
    }

    private void handleCreateTableMsg(CreateTableMsg createTableMsg) {
        // find out how to do layout
        String actorName = createTableMsg.name + "-actor";
        ActorRef table = getContext().actorOf(Table.props(createTableMsg.name, createTableMsg.layout), actorName);
        tables.put(createTableMsg.name, table);
        System.out.println("createTableMsg = [" + createTableMsg.name + ": " + createTableMsg.layout + "]");
        getSender().tell(new QuerySuccessMsg(), getSelf());
    }

    private void handleSelectAllMsg(SelectAllMsg selectAllMsg) {
        tables.get(selectAllMsg.tableName).tell("selectAllMsg", getSender());
    }


    /*
    protected:

    void handleInsert(String tableName, Row row)
    void handleSelectColumn(String tableName, String... columns)
    void handleSelectWhere(String tableName, String column, FilterFn filter)
     */


    // =================================
    //            Messages
    // =================================
    static final class CreateTableMsg {
        String name;
        String layout;

        CreateTableMsg(String name, String layout) {
            this.name = name;
            this.layout = layout;
        }
    }

    public static final class SelectAllMsg {
        String tableName;
        ActorRef requester;

        public SelectAllMsg(String tableName, ActorRef requester) {
            this.tableName = tableName;
            this.requester = requester;
        }
    }

    static final class QuerySuccessMsg {}

    static final class QueryErrorMsg {
        String msg;

        public QueryErrorMsg(String msg) {
            this.msg = msg;
        }
    }

}
