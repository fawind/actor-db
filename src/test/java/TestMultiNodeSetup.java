import com.google.common.collect.ImmutableList;
import configuration.DatastoreModule;
import configuration.EnvConfig;
import core.Datastore;
import model.Row;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class TestMultiNodeSetup {

    private final List<Integer> ports = ImmutableList.of(2552, 2553);
    private static final List<String> defaultLayout = ImmutableList.of("int", "string", "int");
    private List<Datastore> datastores;

    @Before
    public void setup() throws Exception {
        datastores = new ArrayList<>();
        ports.forEach(port -> {
            Datastore store = DatastoreModule.createInstance(EnvConfig.withPort(port));
            datastores.add(store);
            store.startWithCustomClientActor(TestClientActor.props(), format("TestClient-%d", port));
        });
        Thread.sleep(3000);
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(1000);
        datastores.forEach(Datastore::close);
        Thread.sleep(500);
    }

    @Test
    public void testNodeFinding() {
        Datastore store = datastores.get(0);
        store.createTable("foo", defaultLayout);

        for (int i = 0; i < 2; ++i) {
            store.insertInto("foo", new Row(String.valueOf(i), "abc", "23"));
        }

        store.selectAllFrom("foo");
    }
}
