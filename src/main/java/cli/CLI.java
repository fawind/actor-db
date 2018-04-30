package cli;

import cli.commands.Command;
import cli.commands.CreateTableCommand;
import cli.commands.InsertIntoCommand;
import cli.commands.SelectAllCommand;
import configuration.DatastoreModule;
import core.Datastore;
import model.Row;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;

import static java.lang.String.format;

public class CLI {

    private static final String PROMPT = "hakkan-db> ";
    private static final String APP_NAME = "hakkan-db";

    private LineReader lineReader = LineReaderBuilder.builder().appName(APP_NAME).build();

    public static void main(String[] args) {
        new CLI().run();
    }

    public void run() {
        try (Datastore datastore = DatastoreModule.createInstance()) {
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
                    executeCommand(datastore, line);
                } catch (IllegalArgumentException e) {
                    System.out.println(format("Error: %s", e.getMessage()));
                }
            } catch (UserInterruptException | EndOfFileException e) {
                return;
            }
        }
    }

    private void executeCommand(Datastore datastore, String message) {
        if (message == null || message.length() == 0) {
            return;
        }
        Command command = CommandParser.fromLine(message);
        switch (command.getCommandType()) {
            case CREATE_TABLE:
                CreateTableCommand createCommand = (CreateTableCommand) command;
                datastore.createTable(createCommand.getTableName(), createCommand.getSchema());
                break;
            case INSERT_INTO:
                InsertIntoCommand insertCommand = (InsertIntoCommand) command;
                datastore.insertInto(
                        insertCommand.getTableName(),
                        new Row(insertCommand.getValues().toArray(new String[insertCommand.getValues().size()])));
                break;
            case SELECT_ALL:
                SelectAllCommand selectAllCommand = (SelectAllCommand) command;
                datastore.selectAllFrom(selectAllCommand.getTableName());
                break;
            default:
                break;
        }
    }
}
