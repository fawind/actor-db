package cli.commands;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InsertIntoCommand implements Command {

    private final String tableName;
    private final List<String> values;

    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_INTO;
    }
}
