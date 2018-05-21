package messages.quorum;

import messages.query.TransactionMsg;

import java.io.Serializable;

public class QuorumMsg implements Serializable {
    private TransactionMsg transactionMsg;

    public QuorumMsg() {}

    public QuorumMsg(TransactionMsg transactionMsg) {
        this.transactionMsg = transactionMsg;
    }

    public TransactionMsg getTransactionMsg() {
        return transactionMsg;
    }
}
