package messages;

public final class CreateTableMsg {
    private final String name;
    private final String layout;

    public CreateTableMsg(String name, String layout) {
        this.name = name;
        this.layout = layout;
    }

    public String getLayout() {
        return layout;
    }

    public String getName() {
        return name;
    }
}
