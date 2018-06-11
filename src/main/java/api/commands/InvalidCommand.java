package api.commands;

public class InvalidCommand implements Command {

    @Override
    public CommandType getCommandType() {
        return CommandType.INVALID;
    }
}
