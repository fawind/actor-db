package messages.query;

import model.LamportQuery;

import java.util.List;

public final class CreateTableMsg extends LamportQueryMsg {

    private String tableName;
    private List<String> layout;

    private CreateTableMsg() {}

    public CreateTableMsg(String tableName, List<String> layout, LamportQuery lamportQuery) {
        super(lamportQuery);
        this.tableName = tableName;
        this.layout = layout;
    }

    public List<String> getLayout() {
        return layout;
    }

    public String getTableName() {
        return tableName;
    }
}
