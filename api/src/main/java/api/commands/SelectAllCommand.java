package api.commands;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SelectAllCommand implements Command {

    private String tableName;

    private SelectAllCommand() {}

    @Override
    public CommandType getCommandType() {
        return CommandType.SELECT_ALL;
    }
}
