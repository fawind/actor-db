import configuration.DatastoreModule;
import core.Datastore;
import model.Row;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestRunner {
    private Datastore store;
    private String tableName = "foo";
    private List<String> defaultLayout = new ArrayList<>();

    @Before
    public void setUp() {
        store = DatastoreModule.createInstance();
        store.startWithCustomClientActor(TestClientActor.props(), "TestClient");

        defaultLayout.add("int");
        defaultLayout.add("string");
        defaultLayout.add("int");
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(500);
        store.close();
    }

    @Test
    public void testCreateTable() {
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

//    @Test
//    public void testPartitioningLarge() {
//        store.createTable(tableName, defaultLayout);
//        for (int i = 0; i <= 1000; ++i) {
//            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"));
//        }
//    }

}
