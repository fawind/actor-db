package messages.quorum;

import messages.query.LamportQueryMsg;

import java.io.Serializable;

public class QuorumMsg implements Serializable {
    private LamportQueryMsg lamportQueryMsg;

    public QuorumMsg() {}

    public QuorumMsg(LamportQueryMsg lamportQueryMsg) {
        this.lamportQueryMsg = lamportQueryMsg;
    }

    public LamportQueryMsg getLamportQueryMsg() {
        return lamportQueryMsg;
    }
}
