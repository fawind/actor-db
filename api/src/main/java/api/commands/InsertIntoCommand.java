package api.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class InsertIntoCommand implements Command {

    private String tableName;
    private List<String> values;

    private InsertIntoCommand() {}

    @Override
    public CommandType getCommandType() {
        return CommandType.INSERT_INTO;
    }
}
