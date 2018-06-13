package messages.query;

import api.messages.QueryMetaInfo;

public class QuerySuccessMsg extends QueryResponseMsg {

    // Used for serialization
    private QuerySuccessMsg() {}

    public QuerySuccessMsg(QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
    }
}
