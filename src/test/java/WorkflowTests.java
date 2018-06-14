import api.commands.CreateTableCommand;
import client.DatastoreClient;
import client.config.DatastoreClientModule;
import com.google.common.collect.ImmutableList;
import configuration.DatastoreModule;
import api.configuration.EnvConfig;
import core.Datastore;
import messages.query.QueryResponseMsg;
import messages.query.QuerySuccessMsg;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class WorkflowTests {

    private static final EnvConfig DATASTORE_ENV_CONFIG = EnvConfig.withPort(2552);
    private static final EnvConfig CLIENT_ENV_CONFIG = EnvConfig.withPort(2553);

    private static final ImmutableList<String> DEFAULT_SCHEMA = ImmutableList.of("string", "string");

    private Datastore datastore;
    private DatastoreClient client;

    @Before
    public void setup() throws Exception {
        datastore = DatastoreModule.createInstance(DATASTORE_ENV_CONFIG);
        datastore.start();
        client = DatastoreClientModule.createInstance(CLIENT_ENV_CONFIG, DATASTORE_ENV_CONFIG);
        client.start();
        Thread.sleep(2000);
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(2000);
        client.close();
        datastore.close();
    }

    @Test
    public void testCreateTable() throws ExecutionException, InterruptedException {
        // GIVEN
        CreateTableCommand command = CreateTableCommand.builder()
                .tableName("dummyTable")
                .schema(DEFAULT_SCHEMA)
                .build();

        // WHEN
        CompletableFuture<QueryResponseMsg> response = client.sendRequest(command);

        // THEN
        assertThat(response.get()).isInstanceOf(QuerySuccessMsg.class);
    }
}
