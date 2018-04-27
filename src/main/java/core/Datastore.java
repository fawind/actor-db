package core;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import messages.CreateTableMsg;
import messages.InsertMsg;
import messages.SelectAllMsg;
import messages.SelectWhereMsg;

import java.util.function.Predicate;

public class Datastore implements AutoCloseable {

    public static final String SYSTEM_NAME = "actor-db";

    private ActorSystem actorSystem;
    private ActorRef master;
    private ActorRef clientActor;

    public ActorRef start() {
        return startWithCustomClientActor(CLIActor.props(), CLIActor.ACTOR_NAME);
    }

    public ActorRef startWithCustomClientActor(Props props, String name) {
        actorSystem = ActorSystem.create(SYSTEM_NAME);
        master = actorSystem.actorOf(Master.props(), Master.ACTOR_NAME);
        clientActor = actorSystem.actorOf(props, name);
        return clientActor;
    }

    public void createTable(String tableName, String schema) {
        master.tell(new CreateTableMsg(tableName, schema, 0, clientActor), clientActor);
    }

    public void insertInto(String tableName, Row row) {
        master.tell(new InsertMsg(tableName, row, 0, clientActor), clientActor);
    }

    public void selectAllFrom(String tableName) {
        master.tell(new SelectAllMsg(tableName, 0, clientActor), clientActor);
    }

    public void selectFromWhere(String tableName, Predicate<Row> whereFn) {
        master.tell(new SelectWhereMsg(tableName, whereFn, 0, clientActor), clientActor);
    }

    @Override
    public void close() throws Exception {
        actorSystem.terminate();
    }
}
