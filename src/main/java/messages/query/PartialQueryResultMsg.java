package messages.query;

import model.LamportQuery;
import model.StoredRow;

import java.util.List;

public class PartialQueryResultMsg extends QueryResponseMsg {

    private List<StoredRow> result;

    // Used for serialization
    private PartialQueryResultMsg() {}

    public PartialQueryResultMsg(List<StoredRow> result, LamportQuery lamportQuery) {
        super(lamportQuery);
        this.result = result;
    }

    public List<StoredRow> getResult() {
        return result;
    }
}
