package api.commands;

import lombok.Builder;
import lombok.Data;
import model.Row;

import java.util.function.Predicate;

@Data
@Builder
public class SelectWhereCommand implements Command {

    private final String tableName;
    private final Predicate<Row> whereFn;

    @Override
    public CommandType getCommandType() {
        return CommandType.SELECT_WHERE;
    }
}
