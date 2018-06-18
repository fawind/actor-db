package api.commands;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SelectCommand implements Command {

    private final String tableName;
    private final String key;

    @Override
    public CommandType getCommandType() {
        return CommandType.SELECT;
    }
}
