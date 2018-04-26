package messages;

import akka.actor.ActorRef;

public final class SelectAllMsg {
    private final String tableName;
    private final ActorRef requester;

    public SelectAllMsg(String tableName, ActorRef requester) {
        this.tableName = tableName;
        this.requester = requester;
    }

    public String getTableName() {
        return tableName;
    }

    public ActorRef getRequester() {
        return requester;
    }
}
