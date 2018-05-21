package model;

import akka.actor.ActorRef;

public class ReadTransaction extends Transaction {
    public ReadTransaction(long transactionId, ActorRef requester) {
        super(transactionId, requester);
    }
}
