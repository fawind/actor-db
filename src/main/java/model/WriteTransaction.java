package model;

import akka.actor.ActorRef;

public class WriteTransaction extends Transaction {
    public WriteTransaction(long transactionId, ActorRef requester) {
        super(transactionId, requester);
    }
}
