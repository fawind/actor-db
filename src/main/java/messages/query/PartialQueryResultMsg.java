package messages.query;

import api.messages.QueryMetaInfo;
import model.StoredRow;

import java.util.List;

public class PartialQueryResultMsg extends QueryResponseMsg {

    private List<StoredRow> result;

    // Used for serialization
    private PartialQueryResultMsg() {}

    public PartialQueryResultMsg(List<StoredRow> result, QueryMetaInfo queryMetaInfo) {
        super(queryMetaInfo);
        this.result = result;
    }

    public List<StoredRow> getResult() {
        return result;
    }
}
