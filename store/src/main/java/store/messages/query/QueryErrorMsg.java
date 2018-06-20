package store.messages.query;

import api.messages.QueryMetaInfo;

public final class QueryErrorMsg extends QueryResponseMsg {

    private String msg;

    // Used for serialization
    private QueryErrorMsg() {}

    public QueryErrorMsg(String msg, QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
