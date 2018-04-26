package messages;

public final class QueryErrorMsg {
    private final String msg;

    public QueryErrorMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
