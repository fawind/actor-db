package api.messages;

import api.model.Row;

import java.util.List;

public class QueryResultMsg extends QueryResponseMsg {

    private List<Row> result;

    // Used for serialization
    private QueryResultMsg() {}

    public QueryResultMsg(List<Row> result, QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
        this.result = result;
    }

    public List<Row> getResult() {
        return result;
    }
}
