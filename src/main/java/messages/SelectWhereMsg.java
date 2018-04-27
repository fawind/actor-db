package messages;

import akka.actor.ActorRef;
import core.Row;

import java.util.function.Predicate;

public class SelectWhereMsg extends TransactionMsg {
    private final String tableName;
    private final Predicate<Row> whereFn;

    public SelectWhereMsg(String tableName, Predicate<Row> whereFn, long transactionId, ActorRef requester) {
        super(transactionId, requester);
        this.tableName = tableName;
        this.whereFn = whereFn;
    }

    public String getTableName() {
        return tableName;
    }

    public Predicate<Row> getWhereFn() {
        return whereFn;
    }
}
