package messages.query;

import api.messages.LamportQuery;

public class QueryResponseMsg extends LamportQueryMsg {
    protected QueryResponseMsg() {}

    public QueryResponseMsg(LamportQuery lamportQuery) {
        super(lamportQuery);
    }
}
