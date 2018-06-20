package store.messages.query;

import akka.actor.ActorRef;
import api.messages.LamportId;
import api.messages.QueryMetaInfo;

import java.io.Serializable;

public abstract class QueryMsg implements Serializable {

    protected QueryMetaInfo queryMetaInfo;

    // Used for serialization
    protected QueryMsg() {}

    public QueryMsg(QueryMetaInfo queryMetaInfo) {
        this.queryMetaInfo = queryMetaInfo;
    }

    public QueryMetaInfo getQueryMetaInfo() {
        return queryMetaInfo;
    }

    public LamportId getLamportId() {
        return queryMetaInfo.getLamportId();
    }

    public ActorRef getRequester() {
        return queryMetaInfo.getRequester();
    }

    public void updateMetaInfo(QueryMetaInfo queryMetaInfo) {
        this.queryMetaInfo = queryMetaInfo;
    }
}
