import configuration.DatastoreModule;
import core.Datastore;
import core.Row;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestRunner {
    private Datastore store;
    private String tableName = "foo";
    private String defaultLayout = "int,string,int";

    @Before
    public void setUp() {
        store = DatastoreModule.createInstance();
        store.startWithCustomClientActor(TestClientActor.props(), "TestClient");
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(1000);
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
        for (int i = 1; i <= 100; ++i) {
            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"));
        }
    }

    @Test
    public void testSelectAll() {
        store.createTable(tableName, defaultLayout);
        for (int i = 0; i < 10; ++i) {
            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"));
        }

        store.selectAllFrom(tableName);
    }

    @Test
    public void testSelectWhere() {
        store.createTable(tableName, defaultLayout);
        for (int i = 0; i < 10; ++i) {
            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"));
        }

        store.selectFromWhere(tableName, row -> row.getKey().equals("3"));
    }
}
