package api.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DeleteCommand implements Command {

    private String tableName;
    private String key;

    private DeleteCommand() {}

    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE;
    }
}
