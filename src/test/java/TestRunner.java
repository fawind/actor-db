import configuration.DatastoreModule;
import core.Datastore;
import core.Row;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestRunner {
    private Datastore store;

    @Before
    public void setUp() {
        store = DatastoreModule.createInstance();
        store.start();
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(5000);
        store.close();
    }

    @Test
    public void test() {
        String dbName = "foo";
        store.createTable(dbName, "int,int,int");
        for (int i = 0; i < 11; ++i) {
            store.insertInto(dbName, new Row(String.valueOf(i), "abc", "23"));
        }
    }
}
