package cli.commands;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateTableCommand implements Command {

    private final String tableName;
    private final List<String> schema;

    @Override
    public CommandType getCommandType() {
        return CommandType.CREATE_TABLE;
    }
}
