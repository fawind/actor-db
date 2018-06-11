package messages.query;

import model.LamportQuery;

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
