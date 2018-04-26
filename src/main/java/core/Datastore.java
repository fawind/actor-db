package core;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import messages.CreateTableMsg;
import messages.InsertMsg;

public class Datastore implements AutoCloseable {

    public static final String SYSTEM_NAME = "actor-db";

    private ActorSystem actorSystem;
    private ActorRef master;
    private ActorRef cliActor;

    public void start() {
        actorSystem = ActorSystem.create(SYSTEM_NAME);
        master = actorSystem.actorOf(Master.props(), Master.ACTOR_NAME);
        cliActor = actorSystem.actorOf(CLIActor.props(), CLIActor.ACTOR_NAME);
    }

    public void createTable(String tableName, String schema) {
        master.tell(new CreateTableMsg(tableName, schema), cliActor);
    }

    public void insertInto(String tableName, Row row) {
        master.tell(new InsertMsg(tableName, row), cliActor);
    }

    @Override
    public void close() throws Exception {
        actorSystem.terminate();
    }
}
