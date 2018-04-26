import com.google.common.base.Splitter;
import configuration.DatastoreModule;
import core.Datastore;
import core.Row;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;

import java.util.List;

import static java.lang.String.format;

public class CLI {

    private static final String PROMPT = "hakkan-db> ";
    private static final String APP_NAME = "hakkan-db";

    private LineReader lineReader = LineReaderBuilder.builder().appName(APP_NAME).build();

    public static void main(String[] args) {
        new CLI().run();
    }

    public void run() {
        try (Datastore datastore = DatastoreModule.createDatastoreInstance()) {
            datastore.start();
            readLines(datastore);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readLines(Datastore datastore) {
        while (true) {
            try {
                String line = lineReader.readLine(PROMPT);
                try {
                    parseMessage(datastore, line);
                } catch (IllegalArgumentException e) {
                    System.out.println(format("Error: %s", e.getMessage()));
                }
            } catch (UserInterruptException | EndOfFileException e) {
                return;
            }
        }
    }

    private void parseMessage(Datastore datastore, String message) {
        List<String> parts = Splitter.on(" ").omitEmptyStrings().splitToList(message.toLowerCase());
        if (parts.size() == 0) {
            return;
        }
        if (parts.get(0).equals("insert")) {
            // INSERT <table_name> <valA,valB,valC>
            if (parts.size() != 3) {
                throw new IllegalArgumentException("Invalid argument count");
            }
            String tableName = parts.get(1);
            String row = parts.get(2);
            List<String> fields = Splitter.on(",").splitToList(row);
            datastore.insertInto(tableName, new Row(fields.toArray(new String[fields.size()])));
        } else if (parts.get(0).equals("create")) {
            // CREATE <table_name> <typeA,typeB,typeC>
            if (parts.size() != 3) {
                throw new IllegalArgumentException("Invalid argument count");
            }
            String tableName = parts.get(1);
            String schema = parts.get(2);
            datastore.createTable(tableName,schema);
        } else {
            throw new IllegalArgumentException(format("Invalid command %s", parts.get(0)));
        }
    }
}
