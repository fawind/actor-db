package core;

import actors.CLIActor;
import actors.Master;
import actors.QuorumManager;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import configuration.DatastoreConfig;
import messages.query.CreateTableMsg;
import messages.query.DropTableMsg;
import messages.query.InsertMsg;
import messages.query.SelectAllMsg;
import messages.query.SelectWhereMsg;
import model.ReadTransaction;
import model.Row;
import model.Transaction;
import model.WriteTransaction;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class Datastore implements AutoCloseable {

    public static final String SYSTEM_NAME = "actors-db";

    private final DatastoreConfig config;

    private ActorSystem actorSystem;
    private ActorRef clientActor;
    private ActorRef quorumManager;

    private AtomicLong nextTransactionId;

    @Inject
    public Datastore(DatastoreConfig config) {
        this.config = config;
    }

    public ActorRef start() {
        return startWithCustomClientActor(CLIActor.props(), CLIActor.ACTOR_NAME);
    }

    public ActorRef startWithCustomClientActor(Props props, String name) {
        actorSystem = ActorSystem.create(SYSTEM_NAME, config.getAkkaConfig());
        clientActor = actorSystem.actorOf(props, name);

        Set<ActorRef> masters = new HashSet<>();
        masters.add(actorSystem.actorOf(Master.props(), Master.ACTOR_NAME + "_1"));
        masters.add(actorSystem.actorOf(Master.props(), Master.ACTOR_NAME + "_2"));
        masters.add(actorSystem.actorOf(Master.props(), Master.ACTOR_NAME + "_3"));

        quorumManager = actorSystem.actorOf(QuorumManager.props(masters));

        nextTransactionId = new AtomicLong();
        return clientActor;
    }

    public void createTable(String tableName, List<String> schema) {
        tellQuorumManger(new CreateTableMsg(tableName, schema, getNewWriteTransaction()));
    }

    public void dropTable(String tableName) {
        tellQuorumManger(new DropTableMsg(tableName, getNewWriteTransaction()));
    }

    public void insertInto(String tableName, Row row) {
        tellQuorumManger(new InsertMsg(tableName, row, getNewWriteTransaction()));
    }

    public void selectAllFrom(String tableName) {
        tellQuorumManger(new SelectAllMsg(tableName, getNewReadTransaction()));
    }

    public void selectFromWhere(String tableName, Predicate<Row> whereFn) {
        tellQuorumManger(new SelectWhereMsg(tableName, whereFn, getNewReadTransaction()));
    }

    @Override
    public void close() {
        actorSystem.terminate();
    }

    private <MsgType> void tellQuorumManger(MsgType msg) {
        quorumManager.tell(msg, ActorRef.noSender());
    }

    private Transaction getNewReadTransaction() {
        return new ReadTransaction(nextTransactionId.getAndIncrement(), clientActor);
    }

    private Transaction getNewWriteTransaction() {
        return new WriteTransaction(nextTransactionId.getAndIncrement(), clientActor);
    }
}
