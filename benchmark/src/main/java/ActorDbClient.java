import api.commands.InsertIntoCommand;
import api.commands.SelectWhereCommand;
import api.configuration.EnvConfig;
import client.DatastoreClient;
import client.config.DatastoreClientModule;
import com.google.common.collect.ImmutableList;
import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import messages.query.QueryErrorMsg;
import messages.query.QueryResponseMsg;

import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ActorDbClient extends DB {

    private DatastoreClient client;

    @Override
    public void init() {
        Properties props = getProperties();
        int clientPort = Integer.valueOf(props.getProperty("clientPort", "2553"));
        int storePort = Integer.valueOf(props.getProperty("storePort"));
        EnvConfig clientEnvConfig = EnvConfig.withPort(clientPort);
        EnvConfig storeEnvConfig = EnvConfig.withPort(storePort);
        client = DatastoreClientModule.createInstance(clientEnvConfig, storeEnvConfig);
        client.start();
    }

    @Override
    public void cleanup() {
        client.close();
    }

    @Override
    public int read(String table, String key, Set<String> fields, HashMap<String, ByteIterator> result) {
        SelectWhereCommand cmd = SelectWhereCommand.builder()
                .tableName(table)
                .whereFn(row -> row.getKey().equals(key))
                .build();
        CompletableFuture<QueryResponseMsg> response = client.sendRequest(cmd);
        return checkQuerySuccess(response);
    }

    @Override
    public int scan(String table, String startKey, int recordcount, Set<String> fields, Vector<HashMap<String, ByteIterator>> result) {
        // TODO: Implement range queries
        return Status.SUCCESS;
    }

    @Override
    public int update(String table, String key, HashMap<String, ByteIterator> values) {
        InsertIntoCommand cmd = InsertIntoCommand.builder()
                .tableName(table)
                .values(ImmutableList.of(key, values.toString()))
                .build();
        CompletableFuture<QueryResponseMsg> response = client.sendRequest(cmd);
        return checkQuerySuccess(response);
    }

    @Override
    public int insert(String table, String key, HashMap<String, ByteIterator> values) {
        InsertIntoCommand cmd = InsertIntoCommand.builder()
                .tableName(table)
                .values(ImmutableList.of(key, values.toString()))
                .build();
        CompletableFuture<QueryResponseMsg> response = client.sendRequest(cmd);
        return checkQuerySuccess(response);
    }

    @Override
    public int delete(String table, String key) {
        // TODO: Implement delete
        InsertIntoCommand cmd = InsertIntoCommand.builder()
                .tableName(table)
                .values(ImmutableList.of(key, "TOMBSTONE"))
                .build();
        CompletableFuture<QueryResponseMsg> response = client.sendRequest(cmd);
        return checkQuerySuccess(response);
    }

    private int checkQuerySuccess(CompletableFuture<QueryResponseMsg> response) {
        try {
            QueryResponseMsg msg = response.get();
            if (msg instanceof QueryErrorMsg) {
                return Status.ERROR;
            }
            return Status.SUCCESS;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return Status.ERROR;
        }
    }
}
