package store.messages.quorum;

import api.messages.QueryMsg;

import java.io.Serializable;

public class QuorumMsg implements Serializable {
    private QueryMsg queryMsg;

    public QuorumMsg() {}

    public QuorumMsg(QueryMsg queryMsg) {
        this.queryMsg = queryMsg;
    }

    public QueryMsg getQueryMsg() {
        return queryMsg;
    }
}
