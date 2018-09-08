package api.commands;

public class InvalidCommand implements Command {

    private InvalidCommand() {}

    @Override
    public CommandType getCommandType() {
        return CommandType.INVALID;
    }
}
