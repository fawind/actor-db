package messages;

import akka.actor.ActorRef;

public final class CreateTableMsg extends TransactionMsg {
    private final String name;
    private final String layout;

    public CreateTableMsg(String name, String layout, long transactionId, ActorRef requester) {
        super(transactionId, requester);
        this.name = name;
        this.layout = layout;
    }

    public String getLayout() {
        return layout;
    }

    public String getName() {
        return name;
    }
}
