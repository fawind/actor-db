package messages;

import akka.actor.ActorRef;

public final class SelectAllMsg extends TransactionMsg {
    private final String tableName;

    public SelectAllMsg(String tableName, long transactionId, ActorRef requester) {
        super(transactionId, requester);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
