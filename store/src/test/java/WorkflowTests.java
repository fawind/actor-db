import api.commands.CreateTableCommand;
import api.commands.InsertIntoCommand;
import api.commands.SelectAllCommand;
import api.configuration.EnvConfig;
import client.DatastoreClient;
import client.config.DatastoreClientModule;
import com.google.common.collect.ImmutableList;
import store.configuration.DatastoreModule;
import store.Datastore;
import api.messages.QueryErrorMsg;
import api.messages.QueryResponseMsg;
import api.messages.QueryResultMsg;
import api.messages.QuerySuccessMsg;
import api.model.Row;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class WorkflowTests {

    private static final EnvConfig DATASTORE_ENV_CONFIG = EnvConfig.withPort(2552);
    private static final EnvConfig CLIENT_ENV_CONFIG = EnvConfig.withPort(2553);

    private static final ImmutableList<String> DEFAULT_SCHEMA = ImmutableList.of("string", "string");
    private static final String DEFAULT_TABLE_NAME = "dummyTable";
    private static final CreateTableCommand DEFAULT_CREATE_TABLE_CMD = CreateTableCommand.builder()
            .tableName(DEFAULT_TABLE_NAME)
            .schema(DEFAULT_SCHEMA)
            .build();

    private Datastore datastore;
    private DatastoreClient client;

    @Before
    public void setup() throws Exception {
        Thread.sleep(500);
        datastore = DatastoreModule.createInstance(DATASTORE_ENV_CONFIG);
        datastore.start();
        client = DatastoreClientModule.createInstance(CLIENT_ENV_CONFIG, DATASTORE_ENV_CONFIG);
        client.start();
        Thread.sleep(500);
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(1000);
        client.close();
        datastore.close();
    }

    @Test
    public void givenCreateTableCmdThenReceiveSuccessMsg() throws Exception {
        // WHEN
        CompletableFuture<QueryResponseMsg> response = client.sendRequest(DEFAULT_CREATE_TABLE_CMD);

        // THEN
        assertThat(response.get()).isInstanceOf(QuerySuccessMsg.class);
    }

    @Test
    public void givenDuplicateCreateTableCadsThenReceiveErrorMsg() throws Exception {
        // WHEN
        CompletableFuture<QueryResponseMsg> firstResponse = client.sendRequest(DEFAULT_CREATE_TABLE_CMD);
        assertThat(firstResponse.get()).isInstanceOf(QuerySuccessMsg.class);
        CompletableFuture<QueryResponseMsg> secondResponse = client.sendRequest(DEFAULT_CREATE_TABLE_CMD);

        // THEN
        assertThat(secondResponse.get()).isInstanceOf(QueryErrorMsg.class);
    }

    @Test
    public void givenInsertCmdThenReceiveSuccessMsg() throws Exception {
        // GIVEN
        createDefaultTable();

        // WHEN
        List<List<String>> values = IntStream.range(0, 10)
                .mapToObj(i -> ImmutableList.of(String.valueOf(i), "value"))
                .collect(toImmutableList());
        List<CompletableFuture<QueryResponseMsg>> responses = insertValues(DEFAULT_TABLE_NAME, values);

        // THEN
        for (CompletableFuture<QueryResponseMsg> response : responses) {
            assertThat(response.get()).isInstanceOf(QuerySuccessMsg.class);
        }
    }

    @Test
    public void givenManyInsertCmdThenReceiveSuccessMsg() throws Exception {
        // GIVEN
        createDefaultTable();

        // WHEN
        List<List<String>> values = IntStream.range(0, 100)
                .mapToObj(i -> ImmutableList.of(String.valueOf(i), "value"))
                .collect(toImmutableList());
        List<CompletableFuture<QueryResponseMsg>> responses = insertValues(DEFAULT_TABLE_NAME, values);

        // THEN
        for (CompletableFuture<QueryResponseMsg> response : responses) {
            assertThat(response.get()).isInstanceOf(QuerySuccessMsg.class);
        }
    }

    @Test
    public void givenSelectAllCmdThenReceiveAllMatchedRows() throws Exception {
        // GIVEN
        List<List<String>> values = ImmutableList.of(
                ImmutableList.of("1", "val1"),
                ImmutableList.of("2", "val2"),
                ImmutableList.of("3", "val3"));
        createDefaultTable();
        insertValues(DEFAULT_TABLE_NAME, values);

        // WHEN
        SelectAllCommand command = SelectAllCommand.builder()
                .tableName(DEFAULT_TABLE_NAME)
                .build();
        CompletableFuture<QueryResponseMsg> response = client.sendRequest(command);

        // THEN
        assertThat(response.get()).isInstanceOf(QueryResultMsg.class);
        QueryResultMsg queryResponse = (QueryResultMsg) response.get();
        ImmutableList<Row> rows = sortRowsByKey(queryResponse.getResult());
        assertThat(rows).hasSameSizeAs(values);
        for (int i = 0; i < rows.size(); i++) {
            assertThat(rows.get(i).getValues()).isEqualTo(values.get(i));
        }
    }

    private void createDefaultTable() throws ExecutionException, InterruptedException {
        CompletableFuture<QueryResponseMsg> createTableResponse = client.sendRequest(DEFAULT_CREATE_TABLE_CMD);
        assertThat(createTableResponse.get()).isInstanceOf(QuerySuccessMsg.class);
    }

    private List<CompletableFuture<QueryResponseMsg>> insertValues(String tableName, List<List<String>> rows)
            throws ExecutionException, InterruptedException {
        List<CompletableFuture<QueryResponseMsg>> responses = new ArrayList<>();
        for (List<String> row : rows) {
            InsertIntoCommand command = InsertIntoCommand.builder()
                    .tableName(tableName)
                    .values(row)
                    .build();
            responses.add(client.sendRequest(command));
        }
        CompletableFuture.allOf(responses.toArray(new CompletableFuture[0])).get();
        return responses;
    }

    private ImmutableList<Row> sortRowsByKey(List<Row> rows) {
        return rows.stream()
                .sorted(Comparator.comparing(Row::getKey))
                .collect(toImmutableList());
    }
}
