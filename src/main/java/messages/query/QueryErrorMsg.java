package messages.query;

import api.messages.LamportQuery;

public final class QueryErrorMsg extends QueryResponseMsg {

    private String msg;

    // Used for serialization
    private QueryErrorMsg() {}

    public QueryErrorMsg(String msg, LamportQuery lamportQuery) {
        super(lamportQuery);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
