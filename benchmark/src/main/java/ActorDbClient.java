import api.commands.CreateTableCommand;
import api.commands.InsertIntoCommand;
import api.commands.SelectCommand;
import api.configuration.EnvConfig;
import api.messages.QueryErrorMsg;
import api.messages.QueryResponseMsg;
import client.DatastoreClient;
import client.config.DatastoreClientModule;
import com.google.common.collect.ImmutableList;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.DBException;
import com.yahoo.ycsb.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;

public class ActorDbClient extends DB {

    private static final Logger log = LoggerFactory.getLogger(ActorDbClient.class);

    private DatastoreClient client;

    @Override
    public void init() throws DBException {
        Properties props = getProperties();
        int clientPort = Integer.valueOf(props.getProperty("clientPort"));
        String storeIp = String.valueOf(props.getProperty("storeIp"));
        int storePort = Integer.valueOf(props.getProperty("storePort"));
        EnvConfig clientEnvConfig = EnvConfig.withPort(clientPort);
        EnvConfig storeEnvConfig = EnvConfig.withIpAndPort(storeIp, storePort);
        client = DatastoreClientModule.createInstance(clientEnvConfig, storeEnvConfig);
        client.start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new DBException(e);
        }

        String tableName = props.getProperty("tableName");
        if (tableName != null) {
            log.info("Creating table {}", tableName);
            createDefaultTable(tableName);
        }
    }

    @Override
    public void cleanup() {
        client.close();
    }

    @Override
    public Status read(String table, String key, Set<String> fields, Map<String, ByteIterator> result) {
        SelectCommand cmd = SelectCommand.builder()
                .tableName(table)
                .key(key)
                .build();
        CompletableFuture<QueryResponseMsg> response = client.sendRequest(cmd);
        return checkQuerySuccess(response);
    }

    @Override
    public Status scan(String table, String startKey, int recordcount, Set<String> fields, Vector<HashMap<String, ByteIterator>> result) {
        // TODO: Implement range queries
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    public Status update(String table, String key, Map<String, ByteIterator> values) {
        InsertIntoCommand cmd = InsertIntoCommand.builder()
                .tableName(table)
                .values(ImmutableList.of(key, values.toString()))
                .build();
        CompletableFuture<QueryResponseMsg> response = client.sendRequest(cmd);
        return checkQuerySuccess(response);
    }

    @Override
    public Status insert(String table, String key, Map<String, ByteIterator> values) {
        InsertIntoCommand cmd = InsertIntoCommand.builder()
                .tableName(table)
                .values(ImmutableList.of(key, values.toString()))
                .build();
        CompletableFuture<QueryResponseMsg> response = client.sendRequest(cmd);
        return checkQuerySuccess(response);
    }

    @Override
    public Status delete(String table, String key) {
        // TODO: Implement delete
        InsertIntoCommand cmd = InsertIntoCommand.builder()
                .tableName(table)
                .values(ImmutableList.of(key, "TOMBSTONE"))
                .build();
        CompletableFuture<QueryResponseMsg> response = client.sendRequest(cmd);
        return checkQuerySuccess(response);
    }

    private Status checkQuerySuccess(CompletableFuture<QueryResponseMsg> response) {
        try {
            QueryResponseMsg msg = response.get();
            if (msg instanceof QueryErrorMsg) {
                QueryErrorMsg errorMsg = (QueryErrorMsg) msg;
                log.error(errorMsg.getMsg());
                return Status.ERROR;
            }
            return Status.OK;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return Status.ERROR;
        }
    }

    private void createDefaultTable(String tableName) throws DBException {
        CreateTableCommand cmd = CreateTableCommand.builder()
                .tableName(tableName)
                .schema(ImmutableList.of("string", "string"))
                .build();
        CompletableFuture<QueryResponseMsg> response = client.sendRequest(cmd);
        Status status = checkQuerySuccess(response);
        if (status == Status.ERROR) {
            throw new DBException(format("Error creating default table: %s", tableName));
        }
    }
}
