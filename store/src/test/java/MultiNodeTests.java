import api.commands.CreateTableCommand;
import api.commands.InsertIntoCommand;
import api.configuration.EnvConfig;
import api.messages.QueryErrorMsg;
import api.messages.QueryResponseMsg;
import api.messages.QuerySuccessMsg;
import client.DatastoreClient;
import client.config.DatastoreClientModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import store.Datastore;
import store.configuration.DatastoreModule;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.assertj.core.api.Assertions.assertThat;

public class MultiNodeTests {

    private static final ImmutableList<Integer> DATASTORE_PORTS = ImmutableList.of(2552, 2553);
    private static final ImmutableList<Integer> CLIENT_PORTS = ImmutableList.of(3552, 3553);

    private static final List<String> DEFAULT_SCHEMA = ImmutableList.of("string", "string");
    private static final String DEFAULT_TABLE_NAME = "dummyTable";
    private static final CreateTableCommand DEFAULT_CREATE_TABLE_CMD = CreateTableCommand.builder()
            .tableName(DEFAULT_TABLE_NAME)
            .schema(new ArrayList<>(DEFAULT_SCHEMA)) // ImmutableList is not serializable by kryo per default
            .build();

    private ImmutableList<Datastore> datastores;
    private ImmutableList<DatastoreClient> clients;

    @Before
    public void setup() throws Exception {
        Thread.sleep(500);
        ImmutableList.Builder<Datastore> datastoresBuilder = ImmutableList.builder();
        ImmutableList.Builder<DatastoreClient> clientsBuilder = ImmutableList.builder();
        for (int i = 0; i < DATASTORE_PORTS.size(); i++) {
            Datastore datastore = DatastoreModule.createInstance(EnvConfig.withPort(DATASTORE_PORTS.get(i)));
            DatastoreClient client = DatastoreClientModule.createInstance(
                    EnvConfig.withPort(CLIENT_PORTS.get(i)),
                    EnvConfig.withPort(DATASTORE_PORTS.get(i)));
            datastoresBuilder.add(datastore);
            clientsBuilder.add(client);
            datastore.start();
            client.start();
        }
        datastores = datastoresBuilder.build();
        clients = clientsBuilder.build();
        Thread.sleep(1000);
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(1000);
        datastores.forEach(Datastore::close);
        clients.forEach(DatastoreClient::close);
    }

    @Test
    public void givenCreateTableCmdThenReceiveSuccessMsg() throws Exception {
        // WHEN
        CompletableFuture<QueryResponseMsg> response = clients.get(0).sendRequest(DEFAULT_CREATE_TABLE_CMD);

        // THEN
        assertThat(response.get()).isInstanceOf(QuerySuccessMsg.class);
    }

    @Test
    public void givenInsertCmdThenReceiveSuccessMsg() throws Exception {
        // GIVEN
        createDefaultTable(clients.get(0));
        Thread.sleep(500);

        // WHEN
        List<List<String>> values = IntStream.range(0, 10)
                .mapToObj(i -> ImmutableList.of(String.valueOf(i), "value"))
                .collect(toImmutableList());
        List<CompletableFuture<QueryResponseMsg>> responses = insertValues(DEFAULT_TABLE_NAME, values, clients.get(1));

        // THEN
        Thread.sleep(5000);
        for (CompletableFuture<QueryResponseMsg> response : responses) {
            if (response.get() instanceof QueryErrorMsg) {
                System.err.println(((QueryErrorMsg) response.get()).getMsg());
            }
            assertThat(response.get()).isInstanceOf(QuerySuccessMsg.class);
        }
    }

    // =========
    //  HELPER
    // =========

    private void createDefaultTable(DatastoreClient client) throws ExecutionException, InterruptedException {
        CompletableFuture<QueryResponseMsg> createTableResponse = client.sendRequest(DEFAULT_CREATE_TABLE_CMD);
        assertThat(createTableResponse.get()).isInstanceOf(QuerySuccessMsg.class);
    }

    private List<CompletableFuture<QueryResponseMsg>> insertValues(
            String tableName,
            List<List<String>> rows,
            DatastoreClient client) {
        List<CompletableFuture<QueryResponseMsg>> responses = new ArrayList<>();
        for (List<String> row : rows) {
            InsertIntoCommand command = InsertIntoCommand.builder()
                    .tableName(tableName)
                    .values(row)
                    .build();
            responses.add(client.sendRequest(command));
        }
        return responses;
    }
}
