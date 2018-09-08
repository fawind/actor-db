package api.commands;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SelectCommand implements Command {

    private String tableName;
    private String key;

    private SelectCommand() {}

    @Override
    public CommandType getCommandType() {
        return CommandType.SELECT;
    }
}
