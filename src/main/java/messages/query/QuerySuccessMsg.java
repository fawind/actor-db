package messages.query;

import model.LamportQuery;

public class QuerySuccessMsg extends QueryResponseMsg {

    // Used for serialization
    private QuerySuccessMsg() {}

    public QuerySuccessMsg(LamportQuery lamportQuery) {
        super(lamportQuery);
    }
}
