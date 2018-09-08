package api.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CreateTableCommand implements Command {

    private String tableName;
    private List<String> schema;

    private CreateTableCommand() {}

    @Override
    public CommandType getCommandType() {
        return CommandType.CREATE_TABLE;
    }
}
