import api.messages.LamportId;
import com.google.common.collect.ImmutableList;
import configuration.DatastoreModule;
import core.Datastore;
import model.Row;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TestRunner {

    private static final List<String> defaultLayout = ImmutableList.of("int", "string", "int");
    private final LamportId defaultLamportId = new LamportId(0, 1);
    private Datastore store;
    private String tableName = "foo";

    @Before
    public void setUp() throws Exception {
        store = DatastoreModule.createInstance();
        store.startWithCustomClientActor(TestClientActor.props(), "TestClient");
        Thread.sleep(3000);
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(500);
        store.close();
        Thread.sleep(500);
    }

    @Test
    public void testCreateTable() {
        store.createTable(tableName, defaultLayout, defaultLamportId);
    }

    @Test
    public void testCreateDuplicateTable() {
        store.createTable(tableName, defaultLayout, defaultLamportId);
        store.createTable(tableName, defaultLayout, defaultLamportId);
    }

    @Test
    public void testPartitioning() {
        LamportId id = defaultLamportId;
        store.createTable(tableName, defaultLayout, id);
        for (int i = 0; i <= 100; ++i) {
            id = id.increment();
            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"), id);
        }
    }

    @Test
    public void testSelectAll() {
        LamportId id = defaultLamportId;
        store.createTable(tableName, defaultLayout, id);
        for (int i = 0; i <= 100; ++i) {
            id = id.increment();
            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"), id);
        }
        store.selectAllFrom(tableName, id.increment());
    }

    @Test
    public void testSelectWhere() {
        LamportId id = defaultLamportId;
        store.createTable(tableName, defaultLayout, id);
        for (int i = 0; i <= 100; ++i) {
            id = id.increment();
            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"), id);
        }

        store.selectFromWhere(tableName, row -> row.getKey().equals("13"), id.increment());
    }

    @Test
    public void testPartitioningLarge() {
        LamportId id = defaultLamportId;
        store.createTable(tableName, defaultLayout, id);
        for (int i = 0; i <= 1000; ++i) {
            id = id.increment();
            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"), id);
        }
    }


    @Test
    public void testDropTable() {
        LamportId id = defaultLamportId;
        store.createTable(tableName, defaultLayout, id);
        for (int i = 0; i <= 10; ++i) {
            id = id.increment();
            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"), id);
        }
        store.dropTable(tableName, id.increment());
    }
}
