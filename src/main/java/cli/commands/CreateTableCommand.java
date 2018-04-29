package cli.commands;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTableCommand implements Command {

    private final String tableName;
    private final String schema;

    @Override
    public CommandType getCommandType() {
        return CommandType.CREATE_TABLE;
    }
}
