import com.google.common.collect.ImmutableList;
import configuration.DatastoreModule;
import core.Datastore;
import model.Row;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestRunner {

    private static final List<String> defaultLayout = ImmutableList.of("int", "string", "int");

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
        store.createTable(tableName, defaultLayout);
    }

    @Test
    public void testCreateDuplicateTable() {
        store.createTable(tableName, defaultLayout);
        store.createTable(tableName, defaultLayout);
    }

    @Test
    public void testPartitioning() {
        store.createTable(tableName, defaultLayout);
        for (int i = 0; i <= 100; ++i) {
            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"));
        }
    }

    @Test
    public void testSelectAll() {
        store.createTable(tableName, defaultLayout);
        for (int i = 0; i <= 100; ++i) {
            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"));
        }
        store.selectAllFrom(tableName);
    }

    @Test
    public void testSelectWhere() {
        store.createTable(tableName, defaultLayout);
        for (int i = 0; i <= 100; ++i) {
            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"));
        }

        store.selectFromWhere(tableName, row -> row.getKey().equals("13"));
    }

    @Test
    public void testPartitioningLarge() {
        store.createTable(tableName, defaultLayout);
        for (int i = 0; i <= 1000; ++i) {
            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"));
        }
    }


    @Test
    public void testDropTable() {
        store.createTable(tableName, defaultLayout);
        for (int i = 0; i <= 10; ++i) {
            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"));
        }
        store.dropTable(tableName);
    }
}
