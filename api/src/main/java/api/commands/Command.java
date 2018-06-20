package api.commands;

import java.io.Serializable;

public interface Command extends Serializable {
    CommandType getCommandType();
}
