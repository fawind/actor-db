package api.messages;

public class QuerySuccessMsg extends QueryResponseMsg {

    // Used for serialization
    private QuerySuccessMsg() {}

    public QuerySuccessMsg(QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
    }
}
