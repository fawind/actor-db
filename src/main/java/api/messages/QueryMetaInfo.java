package api.messages;

import akka.actor.ActorRef;
import lombok.ToString;

import java.io.Serializable;

@ToString
public class QueryMetaInfo implements Serializable {

    private QueryType queryType;
    private LamportId lamportId;
    private ActorRef requester;
    private String clientRequestId;
    private LamportId responseLamportId = LamportId.INVALID_LAMPORT_ID;

    private QueryMetaInfo(ActorRef requester, LamportId lamportId, String clientRequestId, QueryType queryType) {
        this.requester = requester;
        this.lamportId = lamportId;
        this.clientRequestId = clientRequestId;
        this.queryType = queryType;
    }

    public static QueryMetaInfo newWriteMeta(ActorRef requester, String clientRequestId, LamportId lamportId) {
        return new QueryMetaInfo(requester, lamportId, clientRequestId, QueryType.WRITE);
    }

    public static QueryMetaInfo newReadMeta(ActorRef requester, String clientRequestId, LamportId lamportId) {
        return new QueryMetaInfo(requester, lamportId, clientRequestId, QueryType.READ);
    }

    public LamportId getLamportId() {
        return lamportId;
    }

    public ActorRef getRequester() {
        return requester;
    }

    public String getClientRequestId() {
        return clientRequestId;
    }

    public LamportId getResponseLamportId() {
        return responseLamportId;
    }

    public boolean isWriteQuery() {
        return queryType == QueryType.WRITE;
    }

    public boolean isReadQuery() {
        return queryType == QueryType.READ;
    }

    public QueryMetaInfo copyWithUpdatedLamportId(LamportId lamportId) {
        return new QueryMetaInfo(requester, lamportId, clientRequestId, queryType);
    }

    public QueryMetaInfo copyWithResponseLamportId(LamportId responseLamportId) {
        return new QueryMetaInfo(requester, lamportId, clientRequestId, queryType).withResponseLamportId
                (responseLamportId);
    }

    private QueryMetaInfo withResponseLamportId(LamportId responseLamportId) {
        this.responseLamportId = responseLamportId;
        return this;
    }

    private enum QueryType {WRITE, READ}
}
