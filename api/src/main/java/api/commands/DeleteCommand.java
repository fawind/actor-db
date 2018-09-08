package api.commands;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteCommand implements Command {

    private String tableName;
    private String key;

    private DeleteCommand() {}

    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE;
    }
}
