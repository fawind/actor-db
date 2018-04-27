import akka.actor.ActorRef;
import configuration.DatastoreModule;
import core.Datastore;
import core.Row;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestRunner {
    private Datastore store;
    private String tableName = "foo";

    @Before
    public void setUp() {
        store = DatastoreModule.createInstance();
        store.start();
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(1000);
        store.close();
    }

    @Test
    public void testCreateTable() {
        store.createTable(tableName, "int,int,int");
        store.createTable(tableName, "int,int,int");

    }

    @Test
    public void testPartitioning() {
        store.createTable(tableName, "int,int,int");
        for (int i = 0; i < 100; ++i) {
            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"));
        }
    }

    @Test
    public void testSelectAll() {
        store.createTable(tableName, "int,int,int");
        for (int i = 0; i < 10; ++i) {
            store.insertInto(tableName, new Row(String.valueOf(i), "abc", "23"));
        }

        store.selectAllFrom(tableName);
    }
}
