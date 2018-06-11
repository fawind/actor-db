package messages.query;

import model.LamportQuery;

public class QueryResponseMsg extends LamportQueryMsg {
    protected QueryResponseMsg() {}

    public QueryResponseMsg(LamportQuery lamportQuery) {
        super(lamportQuery);
    }
}
