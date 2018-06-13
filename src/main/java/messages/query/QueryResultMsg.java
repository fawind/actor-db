package messages.query;

import api.messages.LamportQuery;
import model.Row;

import java.util.List;

public class QueryResultMsg extends QueryResponseMsg {

    private List<Row> result;

    // Used for serialization
    private QueryResultMsg() {}

    public QueryResultMsg(List<Row> result, LamportQuery lamportQuery) {
        super(lamportQuery);
        this.result = result;
    }

    public List<Row> getResult() {
        return result;
    }
}
