package store.messages.query;

import api.messages.QueryMetaInfo;

public class QueryResponseMsg extends QueryMsg {
    protected QueryResponseMsg() {}

    public QueryResponseMsg(QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
    }
}
