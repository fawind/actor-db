package api.messages;

public class QueryResponseMsg extends QueryMsg {
    protected QueryResponseMsg() {}

    public QueryResponseMsg(QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
    }
}
