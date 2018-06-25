package client.commands;

import api.commands.Command;
import api.commands.CreateTableCommand;
import api.commands.DeleteCommand;
import api.commands.InsertIntoCommand;
import api.commands.InvalidCommand;
import api.commands.SelectAllCommand;
import com.google.common.base.Splitter;

import java.util.List;

public class CommandParser {

    private static final CommandBuilder commandBuilder = CommandBuilder.builder()
            .match("create", CommandParser::getCreateTableCommand)
            .match("insert", CommandParser::getInsertIntoCommand)
            .match("select", CommandParser::getSelectAllCommand)
            .match("delete", CommandParser::getDeleteCommand)
            .build();

    public static Command fromLine(String line) {
        List<String> parts = Splitter.on(" ").omitEmptyStrings().splitToList(line.toLowerCase());
        if (parts.size() == 0) {
            return new InvalidCommand();
        }
        return commandBuilder.getCommand(parts.get(0), parts);
    }

    /**
     * CREATE {tableName} {typeA,typeB,typeC}
     */
    private static CreateTableCommand getCreateTableCommand(List<String> parts) {
        if (parts.size() != 3) {
            throw new IllegalArgumentException("Invalid argument count");
        }
        List<String> types = Splitter.on(",").splitToList(parts.get(2));
        return CreateTableCommand.builder()
                .tableName(parts.get(1))
                .schema(types)
                .build();
    }

    /**
     * INSERT {tableName} {valA,valB,valC}
     */
    private static InsertIntoCommand getInsertIntoCommand(List<String> parts) {
        if (parts.size() != 3) {
            throw new IllegalArgumentException("Invalid argument count");
        }
        List<String> values = Splitter.on(",").splitToList(parts.get(2));
        return InsertIntoCommand.builder()
                .tableName(parts.get(1))
                .values(values)
                .build();
    }

    /**
     * SELECT {tableName}
     */
    private static SelectAllCommand getSelectAllCommand(List<String> parts) {
        if (parts.size() != 2) {
            throw new IllegalArgumentException("Invalid argument count");
        }
        return SelectAllCommand.builder().tableName(parts.get(1)).build();
    }

    /**
     * DELETE {tableName} {key}
     */
    private static DeleteCommand getDeleteCommand(List<String> parts) {
        if (parts.size() != 3) {
            throw new IllegalArgumentException("Invalid argument count");
        }
        return DeleteCommand.builder().tableName(parts.get(1)).key(parts.get(2)).build();
    }
}
