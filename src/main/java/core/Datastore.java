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
import model.LamportId;
import model.LamportQuery;
import model.ReadLamportQuery;
import model.Row;
import model.WriteLamportQuery;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Predicate;

public class Datastore implements AutoCloseable {

    public static final String SYSTEM_NAME = "actors-db";

    private final DatastoreConfig config;

    private ActorSystem actorSystem;
    private ActorRef clientActor;
    private ActorRef quorumManager;

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
        quorumManager = actorSystem.actorOf(QuorumManager.props(), QuorumManager.ACTOR_NAME);
        actorSystem.actorOf(Master.props(), Master.ACTOR_NAME);

        return clientActor;
    }

    public void createTable(String tableName, List<String> schema, LamportId lamportId) {
        tellQuorumManger(new CreateTableMsg(tableName, schema, getNewWriteQuery(lamportId)));
    }

    public void dropTable(String tableName, LamportId lamportId) {
        tellQuorumManger(new DropTableMsg(tableName, getNewWriteQuery(lamportId)));
    }

    public void insertInto(String tableName, Row row, LamportId lamportId) {
        tellQuorumManger(new InsertMsg(tableName, row, getNewWriteQuery(lamportId)));
    }

    public void selectAllFrom(String tableName, LamportId lamportId) {
        tellQuorumManger(new SelectAllMsg(tableName, getNewReadQuery(lamportId)));
    }

    public void selectFromWhere(String tableName, Predicate<Row> whereFn, LamportId lamportId) {
        tellQuorumManger(new SelectWhereMsg(tableName, whereFn, getNewReadQuery(lamportId)));
    }

    @Override
    public void close() {
        actorSystem.terminate();
    }

    private <MsgType> void tellQuorumManger(MsgType msg) {
        quorumManager.tell(msg, ActorRef.noSender());
    }

    private LamportQuery getNewReadQuery(LamportId lamportId) {
        return new ReadLamportQuery(clientActor, lamportId);
    }

    private LamportQuery getNewWriteQuery(LamportId lamportId) {
        return new WriteLamportQuery(clientActor, lamportId);
    }
}
