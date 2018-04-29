package cli.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CommandBuilder {

    public static class Builder {

        private final Map<String, Function<List<String>, Command>> creators = new HashMap<>();

        public Builder match(String identifier, Function<List<String>, Command> function) {
            creators.put(identifier, function);
            return this;
        }

        public CommandBuilder build() {
            return new CommandBuilder(creators);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Map<String, Function<List<String>, Command>> creators;

    public CommandBuilder(Map<String, Function<List<String>, Command>> creators) {
        this.creators = creators;
    }

    public Command getCommand(String identifier, List<String> token) {
        if (!creators.containsKey(identifier)) {
            return new InvalidCommand();
        }
        return creators.get(identifier).apply(token);
    }
}
