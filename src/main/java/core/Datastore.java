package core;

import actors.CLIActor;
import actors.Master;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import messages.CreateTableMsg;
import messages.InsertMsg;
import messages.SelectAllMsg;
import messages.SelectWhereMsg;
import model.Row;
import model.Transaction;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class Datastore implements AutoCloseable {

    public static final String SYSTEM_NAME = "actors-db";

    private ActorSystem actorSystem;
    private ActorRef master;
    private ActorRef clientActor;

    private AtomicLong nextTransactionId;

    public ActorRef start() {
        return startWithCustomClientActor(CLIActor.props(), CLIActor.ACTOR_NAME);
    }

    public ActorRef startWithCustomClientActor(Props props, String name) {
        actorSystem = ActorSystem.create(SYSTEM_NAME);
        master = actorSystem.actorOf(Master.props(), Master.ACTOR_NAME);
        clientActor = actorSystem.actorOf(props, name);

        nextTransactionId = new AtomicLong();
        return clientActor;
    }

    public void createTable(String tableName, String schema) {
        tellMaster(new CreateTableMsg(tableName, schema, getNextTransaction()));
    }

    public void insertInto(String tableName, Row row) {
        tellMaster(new InsertMsg(tableName, row, getNextTransaction()));
    }

    public void selectAllFrom(String tableName) {
        tellMaster(new SelectAllMsg(tableName, getNextTransaction()));
    }

    public void selectFromWhere(String tableName, Predicate<Row> whereFn) {
        tellMaster(new SelectWhereMsg(tableName, whereFn, getNextTransaction()));
    }

    @Override
    public void close() {
        actorSystem.terminate();
    }

    private <MsgType> void tellMaster(MsgType msg) {
        master.tell(msg, ActorRef.noSender());
    }

    private Transaction getNextTransaction() {
        return new Transaction(nextTransactionId.getAndIncrement(), clientActor);
    }
}
