package cli.commands;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SelectAllCommand implements Command {

    private final String tableName;

    @Override
    public CommandType getCommandType() {
        return CommandType.SELECT_ALL;
    }
}
