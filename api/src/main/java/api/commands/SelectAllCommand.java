package api.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SelectAllCommand implements Command {

    private String tableName;

    private SelectAllCommand() {}

    @Override
    public CommandType getCommandType() {
        return CommandType.SELECT_ALL;
    }
}
